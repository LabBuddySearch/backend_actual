package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.dto.response.stats.GroupDashboardStatsResponse;
import org.example.dto.response.stats.GroupStatsResponse;
import org.example.dto.response.stats.StudentBriefStatsResponse;
import org.example.dto.response.stats.StudentFailedStatsResponse;
import org.example.dto.response.stats.StudentStatsRowResponse;
import org.example.dto.response.stats.StudentTaskStatsDetailResponse;
import org.example.dto.response.stats.TaskDashboardStatsResponse;
import org.example.dto.response.stats.TeacherDashboardStatsResponse;
import org.example.entity.Group;
import org.example.entity.Role;
import org.example.entity.Status;
import org.example.entity.StudentGroup;
import org.example.entity.Submission;
import org.example.entity.Task;
import org.example.entity.User;
import org.example.exception.NotFoundException;
import org.example.repository.GroupRepository;
import org.example.repository.StudentGroupRepository;
import org.example.repository.SubmissionRepository;
import org.example.repository.TaskRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl {

    private final GroupRepository groupRepository;
    private final StudentGroupRepository studentGroupRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final SubmissionRepository submissionRepository;

    @Transactional(readOnly = true)
    public TeacherDashboardStatsResponse getTeacherDashboard(String teacherEmail) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new NotFoundException("User does not exist"));

        List<Group> groups = groupRepository.findByTeacher_IdAndActiveTrue(teacher.getId());
        List<GroupDashboardStatsResponse> groupStats = groups.stream()
                .map(group -> GroupDashboardStatsResponse.builder()
                        .groupId(group.getId())
                        .groupName(group.getName())
                        .tasks(buildTaskDashboardStats(group, teacher))
                        .build())
                .toList();

        return TeacherDashboardStatsResponse.builder()
                .groups(groupStats)
                .build();
    }

    @Transactional(readOnly = true)
    public GroupStatsResponse getGroupStats(Integer groupId, String teacherEmail) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new NotFoundException("User does not exist"));

        Group group = groupRepository.findByIdAndTeacher_IdAndActiveTrue(groupId, teacher.getId())
                .orElseThrow(() -> new NotFoundException("Group does not exist"));

        List<User> students = resolveStudents(group);
        List<Task> groupTasks = taskRepository.findByAssignedGroup_Id(group.getId());

        List<StudentStatsRowResponse> rows = students.stream()
                .map(student -> buildStudentRow(student, groupTasks))
                .toList();

        return GroupStatsResponse.builder()
                .groupId(group.getId())
                .groupName(group.getName())
                .students(rows)
                .build();
    }

    private List<User> resolveStudents(Group group) {
        Map<Integer, User> byId = new LinkedHashMap<>();
        for (StudentGroup membership : studentGroupRepository.findByGroup_Id(group.getId())) {
            byId.put(membership.getStudent().getId(), membership.getStudent());
        }
        for (User user : userRepository.findByRoleAndStudentGroupIgnoreCase(Role.STUDENT, group.getName())) {
            byId.put(user.getId(), user);
        }
        return new ArrayList<>(byId.values());
    }

    private StudentStatsRowResponse buildStudentRow(User student, List<Task> tasks) {
        List<StudentTaskStatsDetailResponse> taskDetails = tasks.stream()
                .map(task -> buildTaskDetail(student, task))
                .toList();

        int solved = (int) taskDetails.stream().filter(t -> "SOLVED".equals(t.getStatus())).count();

        return StudentStatsRowResponse.builder()
                .studentId(student.getId())
                .fullName(student.getFullName())
                .email(student.getEmail())
                .solvedTasksCount(solved)
                .tasks(taskDetails)
                .build();
    }

    private List<TaskDashboardStatsResponse> buildTaskDashboardStats(Group group, User teacher) {
        Map<Integer, Task> tasksById = new LinkedHashMap<>();
        for (Task task : taskRepository.findByAssignedGroup_Id(group.getId())) {
            tasksById.put(task.getId(), task);
        }
        for (Task task : taskRepository.findByAuthor_IdAndAssignToAllTeacherGroupsTrue(teacher.getId())) {
            tasksById.putIfAbsent(task.getId(), task);
        }

        List<User> students = resolveStudents(group);
        return tasksById.values().stream()
                .map(task -> buildTaskDashboardEntry(task, students, group))
                .toList();
    }

    private TaskDashboardStatsResponse buildTaskDashboardEntry(Task task, List<User> students, Group group) {
        List<StudentBriefStatsResponse> solved = new ArrayList<>();
        List<StudentFailedStatsResponse> failed = new ArrayList<>();

        for (User student : students) {
            if (!isStudentEligibleForTask(student, task, group)) {
                continue;
            }
            if (submissionRepository.existsByUser_IdAndTask_IdAndStatus(
                    student.getId(), task.getId(), Status.ACCEPTED)) {
                solved.add(toBriefStudent(student));
                continue;
            }
            String failureReason = resolveFailureReason(student, task);
            if (failureReason != null) {
                failed.add(StudentFailedStatsResponse.builder()
                        .studentId(student.getId())
                        .fullName(student.getFullName())
                        .email(student.getEmail())
                        .failureReason(failureReason)
                        .build());
            }
        }

        return TaskDashboardStatsResponse.builder()
                .taskId(task.getId())
                .taskTitle(task.getTitle())
                .solvedStudents(solved)
                .failedStudents(failed)
                .build();
    }

    private boolean isStudentEligibleForTask(User student, Task task, Group group) {
        if (task.getAssignedStudent() != null) {
            return Objects.equals(task.getAssignedStudent().getId(), student.getId())
                    && studentBelongsToGroup(student, group);
        }
        if (task.getAssignedGroup() != null) {
            return Objects.equals(task.getAssignedGroup().getId(), group.getId())
                    && studentBelongsToGroup(student, group);
        }
        if (Boolean.TRUE.equals(task.getAssignToAllTeacherGroups())) {
            return studentBelongsToGroup(student, group);
        }
        return false;
    }

    private boolean studentBelongsToGroup(User student, Group group) {
        if (student.getStudentGroup() != null && group.getName().equalsIgnoreCase(student.getStudentGroup())) {
            return true;
        }
        return studentGroupRepository.findByStudent_Id(student.getId()).stream()
                .anyMatch(m -> Objects.equals(m.getGroup().getId(), group.getId()));
    }

    private String resolveFailureReason(User student, Task task) {
        if (submissionRepository.existsByUser_IdAndTask_IdAndStatus(
                student.getId(), task.getId(), Status.ACCEPTED)) {
            return null;
        }
        int maxAttempts = task.getMaxAttempts() == null ? 3 : task.getMaxAttempts();
        long attemptsUsed = submissionRepository.countByUser_IdAndTask_Id(student.getId(), task.getId());
        boolean deadlineExpired = task.getDeadlineAt() != null
                && LocalDateTime.now().isAfter(task.getDeadlineAt());
        if (deadlineExpired) {
            return "DEADLINE_EXCEEDED";
        }
        if (attemptsUsed >= maxAttempts) {
            return "ATTEMPTS_EXHAUSTED";
        }
        return null;
    }

    private StudentBriefStatsResponse toBriefStudent(User student) {
        return StudentBriefStatsResponse.builder()
                .studentId(student.getId())
                .fullName(student.getFullName())
                .email(student.getEmail())
                .build();
    }

    private StudentTaskStatsDetailResponse buildTaskDetail(User student, Task task) {
        int maxAttempts = task.getMaxAttempts() == null ? 3 : task.getMaxAttempts();
        List<Submission> submissions = submissionRepository.findByUser_IdAndTask_IdOrderByCreatedAtDesc(
                student.getId(), task.getId());
        int attemptsUsed = submissions.size();
        boolean solved = submissions.stream().anyMatch(s -> s.getStatus() == Status.ACCEPTED);

        String status;
        if (solved) {
            status = "SOLVED";
        } else if (attemptsUsed >= maxAttempts) {
            status = "FAILED";
        } else if (attemptsUsed > 0) {
            status = "IN_PROGRESS";
        } else {
            status = "NOT_STARTED";
        }

        return StudentTaskStatsDetailResponse.builder()
                .taskId(task.getId())
                .taskTitle(task.getTitle())
                .status(status)
                .attemptsUsed(attemptsUsed)
                .maxAttempts(maxAttempts)
                .build();
    }
}
