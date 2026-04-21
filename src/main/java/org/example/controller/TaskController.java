package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.task.EditTaskRequest;
import org.example.dto.request.task.NewTaskRequest;
import org.example.dto.response.task.ListTasksResponse;
import org.example.dto.response.task.TaskResponse;
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

    @PostMapping("/teacher/create")
    @Operation(summary = "Create a new task (TEACHER only)")
    public ResponseEntity<Void> createTask(@Valid @RequestBody NewTaskRequest newTaskRequest,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        taskService.createTask(newTaskRequest, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("")
    @Operation(summary = "Get list of tasks")
    public ResponseEntity<ListTasksResponse> getTasks() {
        return ResponseEntity.ok(taskService.getTasks());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get task by id (STUDENT only)")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<TaskResponse> getStudentTaskById(@PathVariable Integer id) {
        return ResponseEntity.ok(taskService.getTask(id, false));
    }

    @GetMapping("/teacher/{id}")
    @Operation(summary = "Get task by id (TEACHER only)")
    public ResponseEntity<TaskResponse> getTeacherTaskById(@PathVariable Integer id) {
        return ResponseEntity.ok(taskService.getTask(id, true));
    }

    @PutMapping("/teacher/{id}")
    @Operation(summary = "Update task by id (TEACHER only)")
    public ResponseEntity<Void> updateTask(@Valid @RequestBody EditTaskRequest editTaskRequest,
                                           @PathVariable Integer id) {
        taskService.updateTask(id, editTaskRequest);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/teacher/{id}")
    @Operation(summary = "Delete task by id")
    public ResponseEntity<Void> deleteTask(@PathVariable Integer id) {
        taskService.deleteTask(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

