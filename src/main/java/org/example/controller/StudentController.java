package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.dto.response.user.student.StudentContextResponse;
import org.example.dto.response.user.student.StudentProgressResponse;
import org.example.dto.response.user.student.StudentSubmissionHistoryItemResponse;
import org.example.service.impl.StudentContextServiceImpl;
import org.example.service.impl.StudentWorkspaceServiceImpl;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@Tag(name = "Student", description = "Student workspace APIs")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('STUDENT')")
public class StudentController {

    private final StudentContextServiceImpl studentContextService;
    private final StudentWorkspaceServiceImpl studentWorkspaceService;

    @GetMapping("/context")
    @Operation(summary = "Student group and teacher info")
    public ResponseEntity<StudentContextResponse> getContext(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(studentContextService.getContext(userDetails.getUsername()));
    }

    @GetMapping("/progress")
    @Operation(summary = "Student task completion progress")
    public ResponseEntity<StudentProgressResponse> getProgress(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(studentWorkspaceService.getProgress(userDetails.getUsername()));
    }

    @GetMapping("/submissions/history")
    @Operation(summary = "All submission attempts for visible tasks")
    public ResponseEntity<List<StudentSubmissionHistoryItemResponse>> getSubmissionHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(studentWorkspaceService.getSubmissionHistory(userDetails.getUsername()));
    }
}
