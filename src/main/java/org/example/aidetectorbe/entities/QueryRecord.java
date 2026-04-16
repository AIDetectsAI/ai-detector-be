package org.example.aidetectorbe.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@Table(name = "results")
public class QueryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_id")
    private Long id;

    @Column(name = "photo_id", nullable = false)
    private UUID photoId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "model", nullable = false, length = 50)
    private String model;

    @Column(name = "chance", precision = 4, scale = 2, nullable = false)
    private java.math.BigDecimal chance;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

}
