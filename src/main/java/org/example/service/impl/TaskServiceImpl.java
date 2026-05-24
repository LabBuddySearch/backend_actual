package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.task.EditTaskRequest;
import org.example.dto.request.task.NewTaskRequest;
import org.example.dto.response.task.ListTasksResponse;
import org.example.dto.response.task.ShortTaskResponse;
import org.example.dto.response.task.StudentTaskProgressResponse;
import org.example.dto.response.task.SubmissionSummaryResponse;
import org.example.dto.response.task.TaskResponse;
import org.example.entity.Status;
import org.example.entity.Group;
import org.example.entity.Role;
import org.example.entity.Task;
import org.example.entity.TaskCategory;
import org.example.entity.TestCase;
import org.example.entity.User;
import org.example.exception.NotFoundException;
import org.example.mapper.TaskMapper;
import org.example.mapper.TestCaseMapper;
import org.example.repository.GroupRepository;
import org.example.repository.SecurityLogRepository;
import org.example.repository.StudentGroupRepository;
import org.example.repository.SubmissionRepository;
import org.example.repository.TaskRepository;
import org.example.repository.UserRepository;
import org.example.entity.Submission;
import java.time.LocalDateTime;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final TaskMapper taskMapper;
    private final TestCaseMapper testCaseMapper;
    private final StudentGroupRepository studentGroupRepository;
    private final SubmissionRepository submissionRepository;
    private final SecurityLogRepository securityLogRepository;

    @Transactional
    @CacheEvict(cacheNames = {"tasks_list", "task_by_id"}, allEntries = true)
    public void createTask(NewTaskRequest newTaskRequest, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User does not exist"));
        Task task = taskMapper.fromNewTaskRequest(newTaskRequest);
        if (task.getCategory() == null) {
            task.setCategory(TaskCategory.ALGORITHMS);
        }
        task.setTestCases(new ArrayList<>());
        task.setAuthor(user);
        applyAssignmentFromRequest(
                task,
                newTaskRequest.getAssignToAllTeacherGroups(),
                newTaskRequest.getAssignedGroupId(),
                newTaskRequest.getAssignedStudentId(),
                user.getId());
        List<TestCase> testCases = testCaseMapper.fromTestCaseRequestList(newTaskRequest.getTestCases());
        testCases.forEach(task::addTestCase);
        taskRepository.save(task);
    }

    @Transactional
    @CacheEvict(cacheNames = {"tasks_list", "task_by_id"}, allEntries = true)
    public void updateTask(Integer id, EditTaskRequest request, String email) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new NotFoundException("Task does not exist"));
        ensureAuthor(task, email);

        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getTimeLimitMs() != null) {
            task.setTimeLimitMs(request.getTimeLimitMs());
        }
        if (request.getMemoryLimitMb() != null) {
            task.setMemoryLimitMb(request.getMemoryLimitMb());
        }
        if (request.getDeadlineAt() != null) {
            task.setDeadlineAt(request.getDeadlineAt());
        }
        if (request.getMaxAttempts() != null) {
            task.setMaxAttempts(request.getMaxAttempts());
        }
        if (request.getCategory() != null) {
            task.setCategory(request.getCategory());
        }
        if (Boolean.TRUE.equals(request.getAssignToAllTeacherGroups())) {
            task.setAssignToAllTeacherGroups(true);
            task.setAssignedGroup(null);
            task.setAssignedStudent(null);
        } else if (request.getAssignedGroupId() != null || request.getAssignedStudentId() != null) {
            task.setAssignToAllTeacherGroups(false);
            applyAssignment(task, request.getAssignedGroupId(), request.getAssignedStudentId(), task.getAuthor().getId());
        } else if (Boolean.TRUE.equals(request.getClearAssignment())) {
            task.setAssignToAllTeacherGroups(false);
            task.setAssignedGroup(null);
            task.setAssignedStudent(null);
        }
        if (request.getTestCases() != null && !request.getTestCases().isEmpty()) {
            if (task.getTestCases() == null) {
                task.setTestCases(new ArrayList<>());
            }
            task.getTestCases().clear();
            testCaseMapper.fromTestCaseRequestList(request.getTestCases()).forEach(task::addTestCase);
        }
        taskRepository.save(task);
    }

    @Transactional
    @CacheEvict(cacheNames = {"tasks_list", "task_by_id"}, allEntries = true)
    public void deleteTask(Integer id, String email) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new NotFoundException("Task does not exist"));
        ensureAuthor(task, email);

        List<Submission> submissions = submissionRepository.findByTask_Id(id);
        List<Integer> submissionIds = submissions.stream().map(Submission::getId).toList();
        if (!submissionIds.isEmpty()) {
            securityLogRepository.deleteBySubmission_IdIn(submissionIds);
            submissionRepository.deleteAll(submissions);
        }
        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "tasks_list", key = "#email + '_' + #role")
    public ListTasksResponse getTasks(String email, Role role) {
        List<Task> tasks = switch (role) {
            case TEACHER -> {
                User teacher = userRepository.findByEmail(email)
                        .orElseThrow(() -> new NotFoundException("User does not exist"));
                yield taskRepository.findByAuthor_Id(teacher.getId());
            }
            case STUDENT -> {
                User student = userRepository.findByEmail(email)
                        .orElseThrow(() -> new NotFoundException("User does not exist"));
                yield taskRepository.findAll().stream()
                        .filter(t -> isVisibleToStudent(t, student))
                        .toList();
            }
            default -> taskRepository.findAll();
        };

        User studentForStatus = role == Role.STUDENT
                ? userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User does not exist"))
                : null;

        return ListTasksResponse.builder()
                .tasks(tasks.stream()
                        .map(t -> {
                            ShortTaskResponse response = taskMapper.toShortTaskResponse(t);
                            if (t.getAuthor() != null) {
                                response.setAuthor(t.getAuthor().getEmail());
                            }
                            if (t.getCategory() == null) {
                                response.setCategory(TaskCategory.ALGORITHMS);
                            }
                            if (studentForStatus != null) {
                                response.setStudentStatus(resolveStudentTaskStatus(t, studentForStatus));
                            }
                            return response;
                        })
                        .toList())
                .build();
    }

    private String resolveStudentTaskStatus(Task task, User student) {
        if (submissionRepository.existsByUser_IdAndTask_IdAndStatus(
                student.getId(), task.getId(), Status.ACCEPTED)) {
            return "SOLVED";
        }
        int maxAttempts = task.getMaxAttempts() == null ? 3 : task.getMaxAttempts();
        long attemptsUsed = submissionRepository.countByUser_IdAndTask_Id(student.getId(), task.getId());
        boolean deadlineExpired = task.getDeadlineAt() != null
                && LocalDateTime.now().isAfter(task.getDeadlineAt());
        if (deadlineExpired || attemptsUsed >= maxAttempts) {
            return "FAILED";
        }
        if (attemptsUsed > 0) {
            return "IN_PROGRESS";
        }
        return "NOT_STARTED";
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "task_by_id", key = "#id + '_' + #teacher")
    public TaskResponse getTask(Integer id, boolean teacher) {
        return getTask(id, teacher, null);
    }

    @Transactional(readOnly = true)
    public TaskResponse getTask(Integer id, boolean teacher, String studentEmail) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new NotFoundException("Task does not exist"));
        if (!teacher && studentEmail != null) {
            User student = userRepository.findByEmail(studentEmail)
                    .orElseThrow(() -> new NotFoundException("User does not exist"));
            if (!isVisibleToStudent(task, student)) {
                throw new NotFoundException("Task does not exist");
            }
        }
        TaskResponse taskResponse = taskMapper.toTaskResponse(task);
        if (task.getAssignedGroup() != null) {
            taskResponse.setAssignedGroupId(task.getAssignedGroup().getId());
        }
        if (task.getAssignedStudent() != null) {
            taskResponse.setAssignedStudentId(task.getAssignedStudent().getId());
        }
        taskResponse.setAssignToAllTeacherGroups(Boolean.TRUE.equals(task.getAssignToAllTeacherGroups()));
        if (task.getTestCases() == null) {
            taskResponse.setTestCases(List.of());
        } else {
            taskResponse.setTestCases(testCaseMapper.toTestCaseResponseList(teacher ? task.getTestCases() :
                    task.getTestCases().stream().filter(t -> !Boolean.TRUE.equals(t.getIsHidden())).toList()));
        }

        if (!teacher && studentEmail != null) {
            User student = userRepository.findByEmail(studentEmail)
                    .orElseThrow(() -> new NotFoundException("User does not exist"));
            taskResponse.setStudentProgress(buildStudentProgress(task, student));
        }
        return taskResponse;
    }

    public boolean isTaskVisibleToStudent(Task task, User student) {
        return isVisibleToStudent(task, student);
    }

    private StudentTaskProgressResponse buildStudentProgress(Task task, User student) {
        int maxAttempts = task.getMaxAttempts() == null ? 3 : task.getMaxAttempts();
        long attemptsUsed = submissionRepository.countByUser_IdAndTask_Id(student.getId(), task.getId());
        boolean solved = submissionRepository.existsByUser_IdAndTask_IdAndStatus(
                student.getId(), task.getId(), Status.ACCEPTED);
        boolean deadlineExpired = task.getDeadlineAt() != null && LocalDateTime.now().isAfter(task.getDeadlineAt());
        int attemptsRemaining = (int) Math.max(0, maxAttempts - attemptsUsed);
        boolean canSubmit = !deadlineExpired && !solved && attemptsRemaining > 0;

        List<SubmissionSummaryResponse> recent = submissionRepository
                .findByUser_IdAndTask_IdOrderByCreatedAtDesc(student.getId(), task.getId())
                .stream()
                .limit(10)
                .map(this::toSubmissionSummary)
                .toList();

        return StudentTaskProgressResponse.builder()
                .attemptsUsed((int) attemptsUsed)
                .attemptsRemaining(attemptsRemaining)
                .maxAttempts(maxAttempts)
                .deadlineExpired(deadlineExpired)
                .canSubmit(canSubmit)
                .solved(solved)
                .recentSubmissions(recent)
                .build();
    }

    private SubmissionSummaryResponse toSubmissionSummary(Submission submission) {
        return SubmissionSummaryResponse.builder()
                .id(submission.getId())
                .status(submission.getStatus())
                .createdAt(submission.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<SubmissionSummaryResponse> getStudentSubmissionsForTask(Integer taskId, String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new NotFoundException("User does not exist"));
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task does not exist"));
        if (!isVisibleToStudent(task, student)) {
            throw new NotFoundException("Task does not exist");
        }
        return submissionRepository.findByUser_IdAndTask_IdOrderByCreatedAtDesc(student.getId(), task.getId())
                .stream()
                .map(this::toSubmissionSummary)
                .toList();
    }

    private void ensureAuthor(Task task, String email) {
        if (task.getAuthor() == null || !email.equals(task.getAuthor().getEmail())) {
            throw new NotFoundException("Task does not exist");
        }
    }

    private void applyAssignmentFromRequest(
            Task task,
            Boolean assignToAllTeacherGroups,
            Integer groupId,
            Integer studentId,
            Integer teacherId
    ) {
        if (Boolean.TRUE.equals(assignToAllTeacherGroups)) {
            task.setAssignToAllTeacherGroups(true);
            task.setAssignedGroup(null);
            task.setAssignedStudent(null);
            return;
        }
        task.setAssignToAllTeacherGroups(false);
        applyAssignment(task, groupId, studentId, teacherId);
    }

    private void applyAssignment(Task task, Integer groupId, Integer studentId, Integer teacherId) {
        if (studentId != null) {
            User student = userRepository.findById(studentId)
                    .orElseThrow(() -> new NotFoundException("Student does not exist"));
            task.setAssignedStudent(student);
            task.setAssignedGroup(null);
            return;
        }
        if (groupId != null) {
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new NotFoundException("Group does not exist"));
            if (group.getTeacher() == null || !Objects.equals(group.getTeacher().getId(), teacherId)) {
                throw new NotFoundException("Group does not exist");
            }
            task.setAssignedGroup(group);
            task.setAssignedStudent(null);
            return;
        }
        task.setAssignedGroup(null);
        task.setAssignedStudent(null);
    }

    private boolean isVisibleToStudent(Task task, User student) {
        if (task.getAssignedStudent() != null) {
            return Objects.equals(task.getAssignedStudent().getId(), student.getId());
        }
        if (task.getAssignedGroup() != null) {
            if (!Boolean.TRUE.equals(task.getAssignedGroup().getActive())) {
                return false;
            }
            return studentBelongsToGroup(student, task.getAssignedGroup());
        }
        if (Boolean.TRUE.equals(task.getAssignToAllTeacherGroups())) {
            if (task.getAuthor() == null) {
                return false;
            }
            return studentBelongsToAnyTeacherGroup(student, task.getAuthor().getId());
        }
        return false;
    }

    private boolean studentBelongsToAnyTeacherGroup(User student, Integer teacherId) {
        return groupRepository.findByTeacher_IdAndActiveTrue(teacherId).stream()
                .anyMatch(group -> studentBelongsToGroup(student, group));
    }

    private boolean studentBelongsToGroup(User student, Group group) {
        if (student.getStudentGroup() != null && group.getName().equalsIgnoreCase(student.getStudentGroup())) {
            return true;
        }
        return studentGroupRepository.findByStudent_Id(student.getId()).stream()
                .anyMatch(m -> Objects.equals(m.getGroup().getId(), group.getId()));
    }
}
