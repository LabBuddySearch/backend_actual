package org.example.repository.impl;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.task.NewTaskRequest;
import org.example.dto.response.task.ListTasksResponse;
import org.example.dto.response.task.ShortTaskResponse;
import org.example.dto.response.task.TaskResponse;
import org.example.entity.Task;
import org.example.entity.User;
import org.example.exception.NotFoundException;
import org.example.mapper.TaskMapper;
import org.example.mapper.TestCaseMapper;
import org.example.repository.TaskRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final TestCaseMapper testCaseMapper;

    @Transactional
    public void createTask(NewTaskRequest newTaskRequest, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User does not exist"));
        Task task = taskMapper.fromNewTaskRequest(newTaskRequest);
        task.setAuthor(user);
        task.setTestCases(testCaseMapper.fromTestCaseDtoList(newTaskRequest.getTestCaseDtos()));
        taskRepository.save(task);
    }

    @Transactional
    public ListTasksResponse getTasks() {
        List<Task> tasks = taskRepository.findAll();
        return ListTasksResponse.builder()
                .tasks(tasks.stream()
                        .map(t -> {
                            ShortTaskResponse response = taskMapper.toShortTaskResponse(t);
                            response.setAuthor(t.getAuthor().getEmail());
                            return response;
                        })
                        .toList())
                .build();
    }

    @Transactional
    public TaskResponse getTask(Long id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new NotFoundException("Task does not exist"));
        TaskResponse taskResponse = taskMapper.toTaskResponse(task);
        taskResponse.setTestCaseDtos(testCaseMapper.toTestCaseDtoList(task.getTestCases()));
        return taskResponse;
    }


}
