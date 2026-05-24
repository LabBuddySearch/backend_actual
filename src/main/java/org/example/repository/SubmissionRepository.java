package org.example.repository;

import org.example.entity.Status;
import org.example.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Integer> {
    long countByUser_IdAndTask_Id(Integer userId, Integer taskId);

    boolean existsByUser_IdAndTask_IdAndStatus(Integer userId, Integer taskId, Status status);

    List<Submission> findByUser_IdAndTask_IdOrderByCreatedAtDesc(Integer userId, Integer taskId);

    @Query("SELECT s FROM Submission s JOIN FETCH s.task WHERE s.user.id = :userId ORDER BY s.createdAt DESC")
    List<Submission> findByUser_IdOrderByCreatedAtDescWithTask(@Param("userId") Integer userId);

    List<Submission> findByTask_Id(Integer taskId);

    void deleteByTask_Id(Integer taskId);
}

