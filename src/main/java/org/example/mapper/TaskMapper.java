package org.example.mapper;

import org.example.dto.request.task.NewTaskRequest;
import org.example.dto.response.task.ShortTaskResponse;
import org.example.dto.response.task.TaskResponse;
import org.example.entity.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TaskMapper {
    @Mapping(target = "testCases", ignore = true)
    Task fromNewTaskRequest(NewTaskRequest newTaskRequest);

    TaskResponse toTaskResponse(Task task);

    @Mapping(target = "author", ignore = true)
    ShortTaskResponse toShortTaskResponse(Task task);

    List<ShortTaskResponse> toShortTaskResponseList(List<Task> taskList);

}
