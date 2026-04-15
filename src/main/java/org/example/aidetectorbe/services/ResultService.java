package org.example.aidetectorbe.services;

import org.example.aidetectorbe.entities.Result;
import org.example.aidetectorbe.entities.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ResultService {

    Result saveResult(User user, String imageUrl, String model, BigDecimal chance);

    List<Result> getUserResults(UUID userId);
}
