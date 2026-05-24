package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.group.CreateGroupRequest;
import org.example.dto.response.group.PublicGroupsResponse;
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
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "Groups", description = "Study groups management")
public class GroupController {

    private final TeacherServiceImpl teacherService;

    @GetMapping("/available")
    @Operation(summary = "List active groups for student registration (public)")
    public ResponseEntity<PublicGroupsResponse> getAvailableGroups() {
        return ResponseEntity.ok(teacherService.getAvailableGroups());
    }

    @GetMapping
    @PreAuthorize("hasRole('TEACHER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List groups of the current teacher")
    public ResponseEntity<TeacherGroupsResponse> getTeacherGroups(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(teacherService.getGroups(userDetails.getUsername()));
    }

    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a group for the current teacher")
    public ResponseEntity<TeacherGroup> createGroup(@Valid @RequestBody CreateGroupRequest request,
                                                    @AuthenticationPrincipal UserDetails userDetails) {
        TeacherGroup group = teacherService.createGroup(userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(group);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Detach group from teacher and hide its students")
    public ResponseEntity<Void> detachGroup(@PathVariable Integer id,
                                            @AuthenticationPrincipal UserDetails userDetails) {
        teacherService.detachGroup(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }
}
