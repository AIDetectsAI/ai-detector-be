package org.example.aidetectorbe.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AIModelResponse {
    private String result;
    private Double confidence;
    private String modelUsed;
    private Long processingTimeMs;
}
