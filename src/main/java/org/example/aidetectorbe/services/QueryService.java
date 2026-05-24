package org.example.aidetectorbe.services;

public interface QueryService {
    
    void deleteQuery(Long queryId, String login);
    void deleteAllQueries(String login);
}
