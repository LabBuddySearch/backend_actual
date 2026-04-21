package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.dto.request.task.TestCaseRequest;
import org.example.entity.Task;
import org.example.entity.TestCase;
import org.example.exception.NotFoundException;
import org.example.mapper.TestCaseMapper;
import org.example.repository.TaskRepository;
import org.example.repository.TestCaseRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TestCaseServiceImpl {

    private final TestCaseRepository testCaseRepository;
    private final TaskRepository taskRepository;
    private final TestCaseMapper testCaseMapper;

    @Transactional
    @CacheEvict(cacheNames = {"tasks_list", "task_by_id"}, allEntries = true)
    public void createTestCase(TestCaseRequest testCase, Integer taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new NotFoundException("Task with this ID was not found"));
        task.addTestCase(testCaseMapper.fromTestCaseRequest(testCase));
    }

    @Transactional
    @CacheEvict(cacheNames = {"tasks_list", "task_by_id"}, allEntries = true)
    public void updateTestCase(TestCaseRequest request, Integer testCaseId) {
        TestCase testCase = testCaseRepository.findById(testCaseId)
                .orElseThrow(() -> new NotFoundException("Test case with this ID was not found"));
        testCaseMapper.updateTestCaseFromDto(request, testCase);
    }

    @Transactional
    @CacheEvict(cacheNames = {"tasks_list", "task_by_id"}, allEntries = true)
    public void deleteTestCase(Integer testCaseId) {
        TestCase testCase = testCaseRepository.findById(testCaseId)
                .orElseThrow(() -> new NotFoundException("Test case with this ID was not found"));
        testCaseRepository.delete(testCase);
    }
}
