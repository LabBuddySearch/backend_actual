package org.example.dto.response.group;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PublicGroupsResponse {
    private List<PublicGroupOption> groups;
}
