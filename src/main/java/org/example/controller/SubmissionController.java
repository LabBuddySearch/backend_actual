package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.task.SubmitTaskRequest;
import org.example.entity.Submission;
import org.example.facade.SubmissionFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Submissions", description = "Submitting solutions for tasks")
@SecurityRequirement(name = "bearerAuth")
public class SubmissionController {

    private final SubmissionFacade submissionFacade;

    @PostMapping("/{taskId}/submit")
    @Operation(summary = "Submit solution for a task (MVP)")
    public ResponseEntity<Integer> submit(@PathVariable Integer taskId,
                                          @Valid @RequestBody SubmitTaskRequest request,
                                          @AuthenticationPrincipal UserDetails userDetails) {
        Submission submission = submissionFacade.submit(taskId, userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(submission.getId());
    }
}
