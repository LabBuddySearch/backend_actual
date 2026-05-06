package org.example.mapper;

import org.example.dto.response.task.SubmissionResponse;
import org.example.dto.response.task.TestCaseResponse;
import org.example.entity.Submission;
import org.example.entity.TestCase;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SubmissionMapper {
    SubmissionResponse toSubmissionResponse(Submission submission);

    List<SubmissionResponse> toSubmissionListResponse(List<Submission> submissionList);

}
