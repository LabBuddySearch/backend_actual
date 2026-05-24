package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.group.CreateGroupRequest;
import org.example.dto.response.group.PublicGroupOption;
import org.example.dto.response.group.PublicGroupsResponse;
import org.example.dto.response.user.teacher.TeacherGroup;
import org.example.dto.response.user.teacher.TeacherGroupStudentResponse;
import org.example.dto.response.user.teacher.TeacherGroupsResponse;
import org.example.entity.Group;
import org.example.entity.Role;
import org.example.entity.StudentGroup;
import org.example.entity.User;
import org.example.exception.NotFoundException;
import org.example.exception.NotUniqueObjectException;
import org.example.repository.GroupRepository;
import org.example.repository.StudentGroupRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeacherServiceImpl {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final StudentGroupRepository studentGroupRepository;

    @Transactional(readOnly = true)
    public PublicGroupsResponse getAvailableGroups() {
        List<PublicGroupOption> groups = groupRepository.findByActiveTrue().stream()
                .map(g -> PublicGroupOption.builder()
                        .id(g.getId())
                        .code(g.getName())
                        .teacherFullName(g.getTeacher() != null ? g.getTeacher().getFullName() : "")
                        .build())
                .toList();
        return PublicGroupsResponse.builder().groups(groups).build();
    }

    @Transactional(readOnly = true)
    public TeacherGroupsResponse getGroups(String email) {
        User teacher = requireTeacher(email);
        return TeacherGroupsResponse.builder()
                .groups(mapGroups(groupRepository.findByTeacher_IdAndActiveTrue(teacher.getId())))
                .build();
    }

    @Transactional
    public TeacherGroup createGroup(String email, CreateGroupRequest request) {
        User teacher = requireTeacher(email);
        String name = request.getName().trim();
        if (groupRepository.existsByTeacher_IdAndNameIgnoreCaseAndActiveTrue(teacher.getId(), name)) {
            throw new NotUniqueObjectException("Группа с таким кодом уже существует");
        }
        Group group = Group.builder()
                .name(name)
                .teacher(teacher)
                .active(true)
                .build();
        group = groupRepository.save(group);
        return TeacherGroup.builder()
                .id(group.getId())
                .name(group.getName())
                .students(List.of())
                .build();
    }

    @Transactional
    public void detachGroup(String email, Integer groupId) {
        User teacher = requireTeacher(email);
        Group group = groupRepository.findByIdAndTeacher_IdAndActiveTrue(groupId, teacher.getId())
                .orElseThrow(() -> new NotFoundException("Group does not exist"));

        List<StudentGroup> memberships = studentGroupRepository.findByGroup_Id(group.getId());
        for (StudentGroup membership : memberships) {
            User student = membership.getStudent();
            if (student.getStudentGroup() != null
                    && student.getStudentGroup().equalsIgnoreCase(group.getName())) {
                student.setStudentGroup(null);
            }
        }
        studentGroupRepository.deleteByGroup_Id(group.getId());

        List<User> studentsByCode = userRepository.findByRoleAndStudentGroupIgnoreCase(Role.STUDENT, group.getName());
        for (User student : studentsByCode) {
            student.setStudentGroup(null);
        }

        group.setActive(false);
        groupRepository.save(group);
    }

    private User requireTeacher(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User does not exist"));
    }

    private List<TeacherGroup> mapGroups(List<Group> groups) {
        List<TeacherGroup> result = new ArrayList<>();
        for (Group group : groups) {
            List<StudentGroup> memberships = studentGroupRepository.findByGroup_Id(group.getId());
            List<TeacherGroupStudentResponse> students = memberships.stream()
                    .map(m -> TeacherGroupStudentResponse.builder()
                            .id(m.getStudent().getId())
                            .fullName(m.getStudent().getFullName())
                            .email(m.getStudent().getEmail())
                            .build())
                    .toList();
            result.add(TeacherGroup.builder()
                    .id(group.getId())
                    .name(group.getName())
                    .students(students)
                    .build());
        }
        return result;
    }
}
