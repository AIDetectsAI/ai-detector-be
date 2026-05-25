package org.example.aidetectorbe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AIModelResponse {
    private Double certainty;
    private String modelUsed;
    private Long processingTimeMs;
    private String imageUrl;

    public AIModelResponse(Double certainty, String modelUsed, Long processingTimeMs) {
        this.certainty = certainty;
        this.modelUsed = modelUsed;
        this.processingTimeMs = processingTimeMs;
    }
}
