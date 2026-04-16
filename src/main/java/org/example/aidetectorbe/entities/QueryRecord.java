package org.example.aidetectorbe.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "query_records")
public class QueryRecord {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 100)
    private String imageId;

    @Column(nullable = false, length = 50)
    private String userLogin;

    @Column
    private Double certainty;

    @Column
    private String modelUsed;

    @Column
    private Long processingTimeMs;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

}
