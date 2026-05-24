package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.task.EditTaskRequest;
import org.example.dto.request.task.NewTaskRequest;
import org.example.dto.response.task.ListTasksResponse;
import org.example.dto.response.task.TaskResponse;
import org.example.entity.User;
import org.example.exception.NotFoundException;
import org.example.repository.UserRepository;
import org.example.service.impl.TaskServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management and retrieval")
@SecurityRequirement(name = "bearerAuth")
@EnableMethodSecurity
public class TaskController {

    private final TaskServiceImpl taskService;
    private final UserRepository userRepository;

    @PostMapping("/create")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Create a new task (TEACHER only)")
    public ResponseEntity<Void> createTask(@Valid @RequestBody NewTaskRequest newTaskRequest,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        taskService.createTask(newTaskRequest, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Update task (TEACHER only)")
    public ResponseEntity<Void> updateTask(@PathVariable Integer id,
                                           @Valid @RequestBody EditTaskRequest request,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        taskService.updateTask(id, request, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Delete task (TEACHER only)")
    public ResponseEntity<Void> deleteTask(@PathVariable Integer id,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        taskService.deleteTask(id, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("")
    @Operation(summary = "Get list of tasks (role-aware)")
    public ResponseEntity<ListTasksResponse> getTasks(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User does not exist"));
        return ResponseEntity.ok(taskService.getTasks(userDetails.getUsername(), user.getRole()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get student task by id")
    public ResponseEntity<TaskResponse> getStudentTaskById(@PathVariable Integer id,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(taskService.getTask(id, false, userDetails.getUsername()));
    }

    @GetMapping("/{id}/teacher")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get teacher task by id")
    public ResponseEntity<TaskResponse> getTeacherTaskById(@PathVariable Integer id) {
        return ResponseEntity.ok(taskService.getTask(id, true));
    }
}
