package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.dto.response.user.student.StudentProgressResponse;
import org.example.dto.response.user.student.StudentSubmissionHistoryItemResponse;
import org.example.entity.Status;
import org.example.entity.Submission;
import org.example.entity.Task;
import org.example.entity.User;
import org.example.exception.NotFoundException;
import org.example.repository.SubmissionRepository;
import org.example.repository.TaskRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentWorkspaceServiceImpl {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final SubmissionRepository submissionRepository;
    private final TaskServiceImpl taskService;

    @Transactional(readOnly = true)
    public StudentProgressResponse getProgress(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new NotFoundException("User does not exist"));

        List<Task> assigned = taskRepository.findAll().stream()
                .filter(task -> taskService.isTaskVisibleToStudent(task, student))
                .toList();

        int total = assigned.size();
        int solved = (int) assigned.stream()
                .filter(task -> submissionRepository.existsByUser_IdAndTask_IdAndStatus(
                        student.getId(), task.getId(), Status.ACCEPTED))
                .count();

        int percent = total == 0 ? 0 : (solved * 100) / total;

        return StudentProgressResponse.builder()
                .totalTasks(total)
                .solvedTasks(solved)
                .progressPercent(percent)
                .build();
    }

    @Transactional(readOnly = true)
    public List<StudentSubmissionHistoryItemResponse> getSubmissionHistory(String studentEmail) {
        User student = userRepository.findByEmail(studentEmail)
                .orElseThrow(() -> new NotFoundException("User does not exist"));

        return submissionRepository.findByUser_IdOrderByCreatedAtDescWithTask(student.getId())
                .stream()
                .filter(submission -> taskService.isTaskVisibleToStudent(submission.getTask(), student))
                .map(this::toHistoryItem)
                .toList();
    }

    private StudentSubmissionHistoryItemResponse toHistoryItem(Submission submission) {
        return StudentSubmissionHistoryItemResponse.builder()
                .submissionId(submission.getId())
                .taskId(submission.getTask().getId())
                .taskTitle(submission.getTask().getTitle())
                .status(submission.getStatus())
                .language(submission.getLanguage())
                .createdAt(submission.getCreatedAt())
                .build();
    }
}
