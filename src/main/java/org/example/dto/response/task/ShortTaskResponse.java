package org.example.dto.response.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Schema(description = "Short information about the task")
@AllArgsConstructor
@NoArgsConstructor
public class ShortTaskResponse {
    @Schema(
            description = "Task ID",
            example = "1431342"
    )
    private Integer id;

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
