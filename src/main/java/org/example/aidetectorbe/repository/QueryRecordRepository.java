package org.example.aidetectorbe.repository;

import org.example.aidetectorbe.entities.QueryRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface QueryRecordRepository extends JpaRepository<QueryRecord, Long> {
    Optional<QueryRecord> findByPhotoIdAndUserId(UUID photoId, UUID userId);
}
