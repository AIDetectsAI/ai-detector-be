package org.example.aidetectorbe.repository;

import org.example.aidetectorbe.entities.Result;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ResultRepository extends JpaRepository<Result, Integer> {

    List<Result> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID userId);
}
