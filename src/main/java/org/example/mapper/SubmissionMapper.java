package org.example.mapper;

import org.example.dto.response.task.SubmissionResponse;
import org.example.entity.Submission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubmissionMapper {
    @Mapping(target = "stdout", ignore = true)
    @Mapping(target = "stderr", ignore = true)
    @Mapping(target = "message", ignore = true)
    @Mapping(target = "passed", ignore = true)
    @Mapping(target = "syntaxError", ignore = true)
    @Mapping(target = "failedTestIndex", ignore = true)
    @Mapping(target = "attemptsUsed", ignore = true)
    @Mapping(target = "attemptsRemaining", ignore = true)
    SubmissionResponse toSubmissionResponse(Submission submission);

}
