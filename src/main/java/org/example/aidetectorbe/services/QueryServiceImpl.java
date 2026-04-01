package org.example.aidetectorbe.services;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.example.aidetectorbe.repository.QueryRepository;


@Service
@AllArgsConstructor
public class QueryServiceImpl implements QueryService {

    private final QueryRepository queryRepository;
    
    @Override
    public void deleteQuery(Long queryId, String login) {
        if(!existsByIdAndUserLogin(queryId, login)) {
            throw new EntityNotFoundException("Query was not found");
        }
        //delete query here
    }

    @Override
    public boolean existsByIdAndUserLogin(Long queryId, String login) {
        return queryRepository.findByIdAndUserLogin(queryId, login).isPresent();
    }

}
