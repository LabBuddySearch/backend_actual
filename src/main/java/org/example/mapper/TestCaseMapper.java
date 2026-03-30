package org.example.mapper;

import org.example.dto.common.TestCaseDto;
import org.example.entity.TestCase;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TestCaseMapper {
    TestCase fromTestCaseDto(TestCaseDto testCaseDto);

    List<TestCase> fromTestCaseDtoList(List<TestCaseDto> testCaseDtoList);

    TestCaseDto toTestCaseDto(TestCase testCase);

    List<TestCaseDto> toTestCaseDtoList(List<TestCase> testCaseList);
}
