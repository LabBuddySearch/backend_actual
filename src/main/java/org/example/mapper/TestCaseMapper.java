package org.example.mapper;

import org.example.dto.request.task.TestCaseRequest;
import org.example.dto.response.task.TestCaseResponse;
import org.example.entity.TestCase;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TestCaseMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    TestCase fromTestCaseRequest(TestCaseRequest testCaseDto);

    List<TestCase> fromTestCaseRequestList(List<TestCaseRequest> testCaseDtoList);

    TestCaseResponse toTestCaseResponse(TestCase testCase);

    List<TestCaseResponse> toTestCaseResponseList(List<TestCase> testCaseList);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "task", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateTestCaseFromDto(TestCaseRequest dto, @MappingTarget TestCase entity);
}
