package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.task.EditTaskRequest;
import org.example.dto.request.task.NewTaskRequest;
import org.example.dto.response.task.ListTasksResponse;
import org.example.dto.response.task.ShortTaskResponse;
import org.example.dto.response.task.TaskResponse;
import org.example.entity.Task;
import org.example.entity.TestCase;
import org.example.entity.User;
import org.example.exception.NotFoundException;
import org.example.mapper.SubmissionMapper;
import org.example.mapper.TaskMapper;
import org.example.mapper.TestCaseMapper;
import org.example.repository.TaskRepository;
import org.example.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TaskMapper taskMapper;
    private final TestCaseMapper testCaseMapper;
    private final SubmissionMapper submissionMapper;

    @Transactional
    @CacheEvict(cacheNames = {"tasks_list", "task_by_id"}, allEntries = true)
    public void createTask(NewTaskRequest newTaskRequest, String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User does not exist"));
        Task task = taskMapper.fromNewTaskRequest(newTaskRequest);
        task.setAuthor(user);
        List<TestCase> testCases = testCaseMapper.fromTestCaseRequestList(newTaskRequest.getTestCases());
        testCases.forEach(task::addTestCase);
        taskRepository.save(task);
    }

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

    @Cacheable(cacheNames = "task_by_id", key = "{#id, #teacher}")
    public TaskResponse getTask(Integer id, boolean teacher, String email) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new NotFoundException("Task does not exist"));
        TaskResponse taskResponse = taskMapper.toTaskResponse(task);
        taskResponse.setAuthor(task.getAuthor().getFullName());
        taskResponse.setTestCases(testCaseMapper.toTestCaseResponseList(teacher ? task.getTestCases() :
                task.getTestCases().stream().filter(t -> !t.getIsHidden()).toList()));
        if (!teacher) {
            taskResponse.setSubmissions(submissionMapper.toSubmissionListResponse(task.getSubmissions().stream()
                    .filter(s -> s.getUser().getEmail().equals(email))
                    .toList()));
        }
        return taskResponse;
    }

    @Transactional
    @CacheEvict(cacheNames = {"task_by_id", "tasks_list"}, allEntries = true)
    public void updateTask(Integer id, EditTaskRequest updatedTask) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new NotFoundException("Task does not exist"));
        task.getTestCases().clear();
        List<TestCase> testCases = testCaseMapper.fromTestCaseRequestList(updatedTask.getTestCases());
        testCases.forEach(task::addTestCase);
        taskMapper.updateTask(updatedTask, task);
    }

    @Transactional
    @CacheEvict(cacheNames = {"task_by_id", "tasks_list"}, allEntries = true)
    public void deleteTask(Integer id) {
        Task task = taskRepository.findById(id).orElseThrow(() -> new NotFoundException("Task does not exist"));
        taskRepository.delete(task);
    }
}
