package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.task.SubmitTaskRequest;
import org.example.dto.response.task.SubmissionResponse;
import org.example.facade.SubmissionFacade;
import org.example.service.impl.TaskServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Submissions", description = "Submitting solutions for tasks")
@SecurityRequirement(name = "bearerAuth")
public class SubmissionController {

    private final SubmissionFacade submissionFacade;
    private final TaskServiceImpl taskService;

    @PostMapping("/{taskId}/submit")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Submit solution for a task")
    public ResponseEntity<SubmissionResponse> submit(@PathVariable Integer taskId,
                                                     @Valid @RequestBody SubmitTaskRequest request,
                                                     @AuthenticationPrincipal UserDetails userDetails) {
        SubmissionResponse submission = submissionFacade.submit(taskId, userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(submission);
    }

    @GetMapping("/submit/{submitId}")
    @Operation(summary = "Get submission by id")
    public ResponseEntity<SubmissionResponse> get(@PathVariable Integer submitId) {
        return ResponseEntity.ok(submissionFacade.get(submitId));
    }

    @GetMapping("/{taskId}/submissions")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Student submission history for a task")
    public ResponseEntity<List<org.example.dto.response.task.SubmissionSummaryResponse>> listForTask(
            @PathVariable Integer taskId,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.getStudentSubmissionsForTask(taskId, userDetails.getUsername()));
    }
}
