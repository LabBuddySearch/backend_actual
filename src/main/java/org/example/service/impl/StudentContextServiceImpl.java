package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.dto.response.user.student.StudentContextResponse;
import org.example.entity.Group;
import org.example.entity.StudentGroup;
import org.example.entity.User;
import org.example.exception.NotFoundException;
import org.example.repository.GroupRepository;
import org.example.repository.StudentGroupRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudentContextServiceImpl {

    private final UserRepository userRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final GroupRepository groupRepository;

    @Transactional(readOnly = true)
    public StudentContextResponse getContext(String email) {
        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User does not exist"));

        List<StudentGroup> memberships = studentGroupRepository.findByStudent_Id(student.getId());
        Optional<StudentGroup> activeMembership = memberships.stream()
                .filter(m -> Boolean.TRUE.equals(m.getGroup().getActive()))
                .findFirst();
        if (activeMembership.isPresent()) {
            Group group = activeMembership.get().getGroup();
            User teacher = group.getTeacher();
            return StudentContextResponse.builder()
                    .groupName(group.getName())
                    .teacherName(teacher != null ? teacher.getFullName() : null)
                    .teacherEmail(teacher != null ? teacher.getEmail() : null)
                    .build();
        }

        String groupCode = student.getStudentGroup();
        if (groupCode != null && !groupCode.isBlank()) {
            Optional<Group> groupByName = groupRepository.findAll().stream()
                    .filter(g -> Boolean.TRUE.equals(g.getActive()) && groupCode.equalsIgnoreCase(g.getName()))
                    .findFirst();
            if (groupByName.isPresent()) {
                Group group = groupByName.get();
                User teacher = group.getTeacher();
                return StudentContextResponse.builder()
                        .groupName(group.getName())
                        .teacherName(teacher != null ? teacher.getFullName() : null)
                        .teacherEmail(teacher != null ? teacher.getEmail() : null)
                        .build();
            }
            return StudentContextResponse.builder()
                    .groupName(groupCode)
                    .build();
        }

        return StudentContextResponse.builder().build();
    }
}
