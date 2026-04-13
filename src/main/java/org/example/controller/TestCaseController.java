package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.task.NewTaskRequest;
import org.example.dto.request.task.TestCaseRequest;
import org.example.service.impl.TestCaseServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test-cases")
@RequiredArgsConstructor
@Tag(name = "TestCases", description = "Test cases management and retrieval")
@SecurityRequirement(name = "bearerAuth")
public class TestCaseController {

    private final TestCaseServiceImpl testCaseService;

    @PostMapping("/{taskId}")
    @Operation(summary = "Create a new test case (TEACHER only)")
    public ResponseEntity<Void> createTestCase(@Valid @RequestBody TestCaseRequest testCaseRequest,
                                               @PathVariable Integer taskId) {
        testCaseService.createTestCase(testCaseRequest, taskId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update a test case (TEACHER only)")
    public ResponseEntity<Void> updateTestCase(@Valid @RequestBody TestCaseRequest testCaseRequest,
                                               @PathVariable Integer id) {
        testCaseService.updateTestCase(testCaseRequest, id);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a test case (TEACHER only)")
    public ResponseEntity<Void> deleteTestCase(@PathVariable Integer id) {
        testCaseService.deleteTestCase(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
