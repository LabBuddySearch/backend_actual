package org.example.facade;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.task.SubmitTaskRequest;
import org.example.dto.response.task.SubmissionResponse;
import org.example.entity.Status;
import org.example.entity.Submission;
import org.example.entity.Task;
import org.example.entity.TestCase;
import org.example.entity.User;
import org.example.exception.CodeGuardException;
import org.example.exception.NotFoundException;
import org.example.execution.executor.ExecutionResult;
import org.example.execution.factory.ExecutionStrategyFactory;
import org.example.repository.SubmissionRepository;
import org.example.repository.TaskRepository;
import org.example.repository.UserRepository;
import org.example.service.impl.TaskServiceImpl;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class SubmissionFacade {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final SubmissionRepository submissionRepository;
    private final ExecutionStrategyFactory executionStrategyFactory;
    private final TaskServiceImpl taskService;

    @Transactional
    @CacheEvict(cacheNames = "tasks_list", allEntries = true)
    public SubmissionResponse submit(Integer taskId, String userEmail, SubmitTaskRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User does not exist"));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task does not exist"));

        if (!taskService.isTaskVisibleToStudent(task, user)) {
            throw new NotFoundException("Task does not exist");
        }

        validateCanSubmit(task, user);

        int maxAttempts = task.getMaxAttempts() == null ? 3 : task.getMaxAttempts();
        long usedBefore = submissionRepository.countByUser_IdAndTask_Id(user.getId(), task.getId());

        ExecutionResult compileProbe = executionStrategyFactory
                .getStrategy(request.getLanguage())
                .execute(request.getSourceCode(), "", task.getTimeLimitMs() == null ? 2000 : task.getTimeLimitMs(),
                        task.getMemoryLimitMb() == null ? 256 : task.getMemoryLimitMb());

        if (compileProbe.getExitCode() != 0 || (compileProbe.getStderr() != null && !compileProbe.getStderr().isBlank())) {
            return saveSubmission(user, task, request, Status.COMPILATION_ERROR, compileProbe, true, null,
                    (int) usedBefore + 1, maxAttempts,
                    compileProbe.getStderr() != null ? compileProbe.getStderr() : "Синтаксическая ошибка в коде.");
        }

        List<TestCase> tests = task.getTestCases() == null ? List.of() : task.getTestCases();
        if (tests.isEmpty()) {
            throw new CodeGuardException("У задачи нет тестов для проверки", "NO_TEST_CASES");
        }

        int failedIndex = -1;
        ExecutionResult lastRun = compileProbe;
        for (int i = 0; i < tests.size(); i++) {
            TestCase testCase = tests.get(i);
            lastRun = executionStrategyFactory
                    .getStrategy(request.getLanguage())
                    .execute(
                            request.getSourceCode(),
                            testCase.getInputData(),
                            task.getTimeLimitMs() == null ? 2000 : task.getTimeLimitMs(),
                            task.getMemoryLimitMb() == null ? 256 : task.getMemoryLimitMb()
                    );

            if (lastRun.getExitCode() != 0) {
                failedIndex = i + 1;
                break;
            }

            if (!outputsEqual(lastRun.getStdout(), testCase.getExpectedOutput())) {
                failedIndex = i + 1;
                break;
            }
        }

        boolean passed = failedIndex < 0;
        Status status = passed ? Status.ACCEPTED : Status.WRONG_ANSWER;
        String message = passed
                ? "Все тесты пройдены успешно."
                : "Неверный ответ на тесте #" + failedIndex + ".";

        return saveSubmission(user, task, request, status, lastRun, false, passed ? null : failedIndex,
                (int) usedBefore + 1, maxAttempts, message);
    }

    public SubmissionResponse get(Integer submitId) {
        Submission submission = submissionRepository.findById(submitId)
                .orElseThrow(() -> new NotFoundException("Submit does not exist"));

        int maxAttempts = submission.getTask().getMaxAttempts() == null ? 3 : submission.getTask().getMaxAttempts();
        long used = submissionRepository.countByUser_IdAndTask_Id(
                submission.getUser().getId(), submission.getTask().getId());

        return SubmissionResponse.builder()
                .id(submission.getId())
                .status(submission.getStatus())
                .executionTimeMs(submission.getExecutionTimeMs())
                .language(submission.getLanguage())
                .attemptsUsed((int) used)
                .attemptsRemaining(Math.max(0, maxAttempts - (int) used))
                .passed(submission.getStatus() == Status.ACCEPTED)
                .build();
    }

    private void validateCanSubmit(Task task, User user) {
        if (task.getDeadlineAt() != null && LocalDateTime.now().isAfter(task.getDeadlineAt())) {
            throw new CodeGuardException("Дедлайн истёк. Отправка решения недоступна.", "DEADLINE_EXPIRED");
        }
        int maxAttempts = task.getMaxAttempts() == null ? 3 : task.getMaxAttempts();
        long used = submissionRepository.countByUser_IdAndTask_Id(user.getId(), task.getId());
        if (submissionRepository.existsByUser_IdAndTask_IdAndStatus(user.getId(), task.getId(), Status.ACCEPTED)) {
            throw new CodeGuardException("Задача уже решена.", "ALREADY_SOLVED");
        }
        if (used >= maxAttempts) {
            throw new CodeGuardException("Исчерпаны все попытки решения.", "NO_ATTEMPTS_LEFT");
        }
    }

    private SubmissionResponse saveSubmission(
            User user,
            Task task,
            SubmitTaskRequest request,
            Status status,
            ExecutionResult result,
            boolean syntaxError,
            Integer failedTestIndex,
            int attemptsUsed,
            int maxAttempts,
            String message
    ) {
        Submission submission = Submission.builder()
                .user(user)
                .task(task)
                .sourceCode(request.getSourceCode())
                .language(request.getLanguage())
                .status(status)
                .executionTimeMs((int) result.getExecutionTimeMs())
                .build();
        submission = submissionRepository.save(submission);

        return SubmissionResponse.builder()
                .id(submission.getId())
                .status(submission.getStatus())
                .executionTimeMs(submission.getExecutionTimeMs())
                .language(submission.getLanguage())
                .stdout(result.getStdout())
                .stderr(result.getStderr())
                .message(message)
                .passed(status == Status.ACCEPTED)
                .syntaxError(syntaxError)
                .failedTestIndex(failedTestIndex)
                .attemptsUsed(attemptsUsed)
                .attemptsRemaining(Math.max(0, maxAttempts - attemptsUsed))
                .build();
    }

    private boolean outputsEqual(String actual, String expected) {
        return normalize(actual).equals(normalize(expected));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\r\n", "\n").trim();
    }
}
