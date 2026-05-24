package org.example.dto.response.stats;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GroupStatsResponse {
    private Integer groupId;
    private String groupName;
    private List<StudentStatsRowResponse> students;
}
