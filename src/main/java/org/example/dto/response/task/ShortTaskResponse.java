package org.example.dto.response.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Short information about the task")
public class ShortTaskResponse {
    @Schema(
            description = "Task ID",
            example = "1431342"
    )
    private Long id;

    @Schema(
            description = "Task title",
            example = "Быстрая сортировка"
    )
    private String title;

    @Schema(
            description = "Task author",
            example = "Иван Иванов"
    )
    private String author;
}
