package org.example.service.impl;

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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    @CacheEvict(cacheNames = {"tasks_list", "task_by_id"}, allEntries = true)
    public void createTask(NewTaskRequest newTaskRequest, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User does not exist"));
        Task task = taskMapper.fromNewTaskRequest(newTaskRequest);
        task.setAuthor(user);
        task.setTestCases(testCaseMapper.fromTestCaseDtoList(newTaskRequest.getTestCaseDtos()));
        taskRepository.save(task);
    }

    @Transactional
    @Cacheable(cacheNames = "tasks_list")
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
    @Cacheable(cacheNames = "task_by_id", key = "#id")
    public TaskResponse getTask(Integer id, boolean teacher) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new NotFoundException("Task does not exist"));
        TaskResponse taskResponse = taskMapper.toTaskResponse(task);
        taskResponse.setTestCaseDtos(testCaseMapper.toTestCaseDtoList(teacher ? task.getTestCases():
                task.getTestCases().stream().filter(t -> !t.getIsHidden()).toList()));
        return taskResponse;
    }


}
