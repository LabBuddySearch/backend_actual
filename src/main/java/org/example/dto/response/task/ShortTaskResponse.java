package org.example.dto.response.task;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShortTaskResponse {
    private Long id;

    private String title;

    private String author;
}
