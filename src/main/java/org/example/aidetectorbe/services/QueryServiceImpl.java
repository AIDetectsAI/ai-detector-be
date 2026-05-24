package org.example.aidetectorbe.services;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.example.aidetectorbe.repository.QueryRepository;
import org.example.aidetectorbe.entities.Query;
import java.util.Optional;
import java.util.Set;


@Service
@AllArgsConstructor
public class QueryServiceImpl implements QueryService {

    private final QueryRepository queryRepository;
    
    @Override
    public void deleteQuery(Long queryId, String login) {
        Optional<Query> op_query = queryRepository.findByIdAndUser_Login(queryId, login);
        if(!op_query.isPresent()) {
            throw new EntityNotFoundException("Query was not found");
        }
        Query query = op_query.get();
        query.setIsDeleted(true);
        queryRepository.save(query);
    }

    @Override
    public void deleteAllQueries(String login) {
        Optional<Set<Query>> op_queries = queryRepository.findByUser_Login(login);
        if (op_queries.isEmpty() || op_queries.get().isEmpty()) {
            throw new EntityNotFoundException("Queries were not found");
        }
        Set<Query> queries = op_queries.get();
        for (Query query : queries) {
            query.setIsDeleted(true);
            queryRepository.save(query);
        }
    }

}
