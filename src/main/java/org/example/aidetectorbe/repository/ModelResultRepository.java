package org.example.aidetectorbe.repository;

import org.example.aidetectorbe.entities.ModelResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ModelResultRepository extends JpaRepository<ModelResult, Integer> {
    List<ModelResult> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(UUID userId);

    Optional<ModelResult> findByResultIdAndUserIdAndIsDeletedFalse(UUID resultId, UUID userId);
}