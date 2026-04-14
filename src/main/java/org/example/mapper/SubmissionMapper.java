package org.example.mapper;

import org.example.dto.response.task.SubmissionResponse;
import org.example.entity.Submission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubmissionMapper {
    @Mapping(target = "stdout", ignore = true)
    @Mapping(target = "stderr", ignore = true)
    SubmissionResponse toSubmissionResponse(Submission submission);

}
