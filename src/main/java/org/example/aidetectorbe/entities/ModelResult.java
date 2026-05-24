package org.example.aidetectorbe.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "results")
public class ModelResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID resultId;

    @Column(name = "photo_id", nullable = false)
    private UUID photoId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "model", nullable = false, length = 50)
    private String model;

    @Column(name = "chance", nullable = false, precision = 4, scale = 2)
    private BigDecimal chance;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (isDeleted == null) {
            isDeleted = false;
        }
    }
}

