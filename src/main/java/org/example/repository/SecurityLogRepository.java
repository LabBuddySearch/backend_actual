package org.example.repository;

import org.example.entity.SecurityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public interface SecurityLogRepository extends JpaRepository<SecurityLog, Integer> {

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM SecurityLog sl WHERE sl.submission.id IN :submissionIds")
    void deleteBySubmission_IdIn(@Param("submissionIds") Collection<Integer> submissionIds);
}
