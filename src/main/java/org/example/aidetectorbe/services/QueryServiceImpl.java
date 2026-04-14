package org.example.aidetectorbe.services;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.example.aidetectorbe.repository.QueryRepository;
import org.example.aidetectorbe.entities.Query;


@Service
@AllArgsConstructor
public class QueryServiceImpl implements QueryService {

    private final QueryRepository queryRepository;
    
    @Override
    public void deleteQuery(Long queryId, String login) {
        if(!existsByIdAndUserLogin(queryId, login)) {
            throw new EntityNotFoundException("Query was not found");
        }
        Query query = queryRepository.findByIdAndUser_Login(queryId, login).get();
        query.setIsDeleted(true);
        queryRepository.save(query);
    }

    @Override
    public boolean existsByIdAndUserLogin(Long queryId, String login) {
        return queryRepository.findByIdAndUser_Login(queryId, login).isPresent();
    }

}
