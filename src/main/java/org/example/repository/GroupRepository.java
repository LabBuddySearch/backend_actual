package org.example.repository;

import org.example.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Integer> {
    List<Group> findByTeacher_IdAndActiveTrue(Integer teacherId);

    Optional<Group> findByIdAndTeacher_IdAndActiveTrue(Integer id, Integer teacherId);

    boolean existsByTeacher_IdAndNameIgnoreCaseAndActiveTrue(Integer teacherId, String name);

    List<Group> findByActiveTrue();
}
