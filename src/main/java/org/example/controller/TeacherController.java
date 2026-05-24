package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.group.CreateGroupRequest;
import org.example.dto.response.user.teacher.TeacherGroup;
import org.example.dto.response.user.teacher.TeacherGroupsResponse;
import org.example.service.impl.TeacherServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
@Tag(name = "Teacher", description = "Teacher workspace APIs")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('TEACHER')")
public class TeacherController {

    private final TeacherServiceImpl teacherService;

    @GetMapping("/groups")
    @Operation(summary = "List teacher groups with students")
    public ResponseEntity<TeacherGroupsResponse> getGroups(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(teacherService.getGroups(userDetails.getUsername()));
    }

    @PostMapping("/groups")
    @Operation(summary = "Create a new group for the current teacher")
    public ResponseEntity<TeacherGroup> createGroup(@Valid @RequestBody CreateGroupRequest request,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        TeacherGroup group = teacherService.createGroup(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(group);
    }

    @DeleteMapping("/groups/{id}")
    @Operation(summary = "Detach group from teacher and hide its students")
    public ResponseEntity<Void> detachGroup(@PathVariable Integer id,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        teacherService.detachGroup(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
