package org.example.aidetectorbe.services;

import org.example.aidetectorbe.entities.Result;
import org.example.aidetectorbe.entities.User;
import org.example.aidetectorbe.repository.ResultRepository;
import org.example.aidetectorbe.utils.logger.Log;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ResultServiceImpl implements ResultService {

    private final ResultRepository resultRepository;

    public ResultServiceImpl(ResultRepository resultRepository) {
        this.resultRepository = resultRepository;
    }

    @Override
    public Result saveResult(User user, String imageUrl, String model, BigDecimal chance) {
        Result result = new Result(imageUrl, user, model, chance);
        Result saved = resultRepository.save(result);
        Log.info("Result saved for user " + user.getLogin() + " with id " + saved.getResultId());
        return saved;
    }

    @Override
    public List<Result> getUserResults(UUID userId) {
        return resultRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId);
    }
}
