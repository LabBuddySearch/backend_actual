package org.example.facade;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.task.SubmitTaskRequest;
import org.example.dto.response.task.SubmissionResponse;
import org.example.entity.Status;
import org.example.entity.Submission;
import org.example.entity.Task;
import org.example.entity.User;
import org.example.execution.executor.ExecutionResult;
import org.example.execution.factory.ExecutorFactory;
import org.example.exception.NotFoundException;
import org.example.mapper.SubmissionMapper;
import org.example.repository.SubmissionRepository;
import org.example.repository.TaskRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SubmissionFacade {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final SubmissionRepository submissionRepository;
    private final ExecutorFactory executorFactory;
    private final SubmissionMapper submissionMapper;

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

        ExecutionResult result = executorFactory
                .getExecutor(submission.getLanguage())
                .execute(submission.getSourceCode(), "", task.getTimeLimitMs() == null ? 2000 : task.getTimeLimitMs(),
                        task.getMemoryLimitMb() == null ? 256 : task.getMemoryLimitMb());

        submission.setExecutionTimeMs((int) result.getExecutionTimeMs());
        submission.setStatus(result.getExitCode() == 0 ? Status.ACCEPTED : Status.WRONG);
        return submissionMapper.toSubmissionResponse(submission);
    }

    public SubmissionResponse get(Integer submitId){
        Submission submission = submissionRepository.findById(submitId)
                .orElseThrow(() -> new NotFoundException("Submit does not exist"));

        return submissionMapper.toSubmissionResponse(submission);
    }
}

