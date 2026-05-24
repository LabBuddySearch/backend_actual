package org.example.dto.response.group;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PublicGroupOption {
    private Integer id;
    private String code;
    private String teacherFullName;
}
