package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.task.SubmitTaskRequest;
import org.example.dto.response.task.SubmissionResponse;
import org.example.facade.SubmissionFacade;
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

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
@Tag(name = "Submissions", description = "Submitting solutions for tasks")
@SecurityRequirement(name = "bearerAuth")
public class SubmissionController {

    private final SubmissionFacade submissionFacade;

    @PostMapping("/{taskId}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Submit solution for a task (MVP, синхронное исполнение)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Проверка завершена, в теле — итоговый вердикт"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации, некорректный запрос"),
            @ApiResponse(responseCode = "401", description = "Нет или невалидный JWT"),
            @ApiResponse(responseCode = "403", description = "Нужна роль STUDENT"),
            @ApiResponse(responseCode = "404", description = "Задача или язык не найдены"),
            @ApiResponse(responseCode = "429", description = "Превышен лимит отправок (см. application.rate-limit)"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    public ResponseEntity<SubmissionResponse> submit(@PathVariable Integer taskId,
                                                     @Valid @RequestBody SubmitTaskRequest request,
                                                     @AuthenticationPrincipal UserDetails userDetails) {
        SubmissionResponse submission = submissionFacade.submit(taskId, userDetails.getUsername(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(submission);
    }

    @GetMapping("/teacher/{id}")
    @Operation(summary = "Get solution for a task (MVP) (TEACHER ONLY)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешное получение результата"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации, некорректный запрос"),
            @ApiResponse(responseCode = "404", description = "Решение не найдено"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PreAuthorize("hasRole('TEACHER')")
    public ResponseEntity<SubmissionResponse> getByTeacher(@PathVariable Integer id) {
        SubmissionResponse submission = submissionFacade.get(id, true, null);
        return ResponseEntity.status(HttpStatus.OK).body(submission);
    }

    @GetMapping("/student/{id}")
    @Operation(summary = "Get solution for a task (MVP) (STUDENT ONLY)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешное получение результата"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации, некорректный запрос"),
            @ApiResponse(responseCode = "404", description = "Решение не найдено"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<SubmissionResponse> getByStudent(@PathVariable Integer id,
                                                           @AuthenticationPrincipal UserDetails userDetails) {
        SubmissionResponse submission = submissionFacade.get(id, false, userDetails);
        return ResponseEntity.status(HttpStatus.OK).body(submission);
    }
}
