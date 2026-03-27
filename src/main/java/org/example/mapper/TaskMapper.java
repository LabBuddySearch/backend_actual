package org.example.mapper;

import org.example.dto.request.task.NewTaskRequest;
import org.example.dto.response.task.ShortTaskResponse;
import org.example.dto.response.task.TaskResponse;
import org.example.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskMapper {
    Task fromNewTaskRequest(NewTaskRequest newTaskRequest);

    @Mapping(target = "author", ignore = true)
    TaskResponse toTaskResponse(Task task);

    @Mapping(target = "author", ignore = true)
    ShortTaskResponse toShortTaskResponse(Task task);

}
