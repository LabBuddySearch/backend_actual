package org.example.dto.response.task;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@Schema(description = "List of task data")
public class ListTasksResponse {
    @Schema(
            description = "List tasks",
            example = """
                    [
                        {
                            "id": "31231231",
                            "title": "Быстрая сортировка",
                            "author": "Иван Иванов"
                        }
                    ]
                    """
    )
    private List<ShortTaskResponse> tasks;
}
