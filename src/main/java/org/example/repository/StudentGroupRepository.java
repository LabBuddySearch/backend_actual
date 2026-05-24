package org.example.repository;

import org.example.entity.StudentGroup;
import org.example.entity.StudentGroupId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentGroupRepository extends JpaRepository<StudentGroup, StudentGroupId> {
    List<StudentGroup> findByGroup_Id(Integer groupId);

    List<StudentGroup> findByStudent_Id(Integer studentId);

    void deleteByGroup_Id(Integer groupId);
}
