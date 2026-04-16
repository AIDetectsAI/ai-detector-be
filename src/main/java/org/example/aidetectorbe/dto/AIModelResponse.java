package org.example.aidetectorbe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AIModelResponse {
    private Double certainty;
    private String modelUsed;
    private Long processingTimeMs;
    private String imageId;

    public AIModelResponse(Double certainty, String modelUsed, Long processingTimeMs) {
        this.certainty = certainty;
        this.modelUsed = modelUsed;
        this.processingTimeMs = processingTimeMs;
        this.imageId = null;
    }
}
