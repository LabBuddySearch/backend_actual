package org.example.dto.request.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Data for submit task")
public class SubmitTaskRequest {
    @NotBlank
    @Schema(
            description = "Programming language (исполнитель: JAVA или PYTHON, верхний регистр)",
            example = "JAVA",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String language;

    @Schema(
            description = "Исходный код. Пустая строка допускается: вернётся статус EMPTY_SOURCE без запуска в песочнице.",
            example = "public class Main{\npublic static void main(String[] args) {\nSystem.out.println(\"Hello\");\n}\n}",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private String sourceCode;
}
