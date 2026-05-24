package org.example.dto.response.stats;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TeacherDashboardStatsResponse {
    private List<GroupDashboardStatsResponse> groups;
}
