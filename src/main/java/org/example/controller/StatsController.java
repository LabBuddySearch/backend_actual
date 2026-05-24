package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.dto.response.stats.GroupStatsResponse;
import org.example.dto.response.stats.TeacherDashboardStatsResponse;
import org.example.service.impl.StatsServiceImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@Tag(name = "Statistics", description = "Teacher statistics")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('TEACHER')")
public class StatsController {

    private final StatsServiceImpl statsService;

    @GetMapping("/teacher/dashboard")
    @Operation(summary = "Teacher dashboard: per-group task completion breakdown")
    public ResponseEntity<TeacherDashboardStatsResponse> getTeacherDashboard(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(statsService.getTeacherDashboard(userDetails.getUsername()));
    }

    @GetMapping("/groups/{groupId}")
    @Operation(summary = "Statistics for a teacher group")
    public ResponseEntity<GroupStatsResponse> getGroupStats(@PathVariable Integer groupId,
                                                              @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(statsService.getGroupStats(groupId, userDetails.getUsername()));
    }
}
