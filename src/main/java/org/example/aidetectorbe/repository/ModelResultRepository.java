package org.example.aidetectorbe.repository;

import org.example.aidetectorbe.entities.ModelResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ModelResultRepository extends JpaRepository<ModelResult, Integer> {
	Optional<ModelResult> findByPhotoIdAndUserId(UUID photoId, UUID userId);
}