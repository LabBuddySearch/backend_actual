package org.example.facade;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.task.SubmitTaskRequest;
import org.example.dto.response.task.SubmissionResponse;
import org.example.entity.Status;
import org.example.entity.Submission;
import org.example.entity.Task;
import org.example.entity.User;
import org.example.execution.service.ExecutionResultParserService;
import org.example.execution.executor.ExecutionResult;
import org.example.execution.factory.ExecutionStrategyFactory;
import org.example.exception.NotFoundException;
import org.example.mapper.SubmissionMapper;
import org.example.repository.SubmissionRepository;
import org.example.repository.TaskRepository;
import org.example.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class SubmissionFacade {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final SubmissionRepository submissionRepository;
    private final ExecutionStrategyFactory strategyFactory;
    private final SubmissionMapper submissionMapper;
    private final ExecutionResultParserService resultParserService;

    /**
     * Оркестратор процесса отправки решения.
     * Сейчас MVP: сохраняем submission и запускаем выполнение синхронно (мок-исполнитель уже есть).
     * Позже: очередь, sandbox, security-логи, античит, ретраи.
     */
    @Transactional
    public SubmissionResponse submit(Integer taskId, String userEmail, SubmitTaskRequest request) {


        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new NotFoundException("User does not exist"));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task does not exist"));

        Submission submission = Submission.builder()
                .user(user)
                .task(task)
                .sourceCode(request.getSourceCode())
                .language(request.getLanguage())
                .status(Status.PENDING)
                .build();

        submission = submissionRepository.save(submission);

        if (request.getSourceCode().isEmpty()){
            return submissionMapper.toSubmissionResponse(submission);
        }

        Status finalStatus = Status.ACCEPTED;
        ExecutionResult lastResult = null;

        for (var testCase : task.getTestCases()) {
            lastResult = strategyFactory
                    .getStrategy(submission.getLanguage())
                    .execute(
                            submission.getSourceCode(),
                            testCase.getInputData(),
                            task.getTimeLimitMs() == null ? 2000 : task.getTimeLimitMs(),
                            task.getMemoryLimitMb() == null ? 256 : task.getMemoryLimitMb()
                    );

            Status testStatus = resultParserService.resolveStatus(lastResult, testCase.getExpectedOutput());
            if (testStatus != Status.ACCEPTED) {
                finalStatus = testStatus;
                break;
            }
        }

        if (lastResult == null) {
            finalStatus = Status.INTERNAL_ERROR;
            submission.setStdout("");
            submission.setStderr("No test cases found for task");
            submission.setExecutionTimeMs(0);
        } else {
            submission.setStdout(lastResult.getStdout());
            submission.setStderr(lastResult.getStderr());
            submission.setExecutionTimeMs((int) lastResult.getExecutionTimeMs());
        }

        submission.setStatus(finalStatus);
        submissionRepository.save(submission);
        return submissionMapper.toSubmissionResponse(submission);
    }

    public SubmissionResponse get(Integer submitId, boolean teacher, UserDetails userDetails){
        Submission submission = submissionRepository.findById(submitId)
                .orElseThrow(() -> new NotFoundException("Submit does not exist"));
        if (!teacher && !submission.getUser().getEmail().equals(userDetails.getUsername())){
            throw new NotFoundException("Submit not found");
        }
        return submissionMapper.toSubmissionResponse(submission);
    }
}

