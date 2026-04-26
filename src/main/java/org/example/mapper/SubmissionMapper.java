package org.example.mapper;

import org.example.dto.response.task.SubmissionResponse;
import org.example.entity.Submission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubmissionMapper {
    SubmissionResponse toSubmissionResponse(Submission submission);

}
