package org.example.repository;

import org.example.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    List<Task> findByAuthor_Id(Integer authorId);

    List<Task> findByAssignedGroup_Id(Integer groupId);

    List<Task> findByAuthor_IdAndAssignToAllTeacherGroupsTrue(Integer authorId);
}
