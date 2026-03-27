package org.example.dto.response.task;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ListTasksResponse {
    private List<ShortTaskResponse> tasks;
}
