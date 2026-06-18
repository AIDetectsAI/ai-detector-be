package org.example.aidetectorbe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@AllArgsConstructor
public class HistoryEntryDTO {
    private UUID id;
    private String photoUrl;
    private String model;
    private BigDecimal chance;
    private Instant createdAt;
    private String caption;
}
