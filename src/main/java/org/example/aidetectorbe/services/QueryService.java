package org.example.aidetectorbe.services;

public interface QueryService {
    
    void deleteQuery(Long queryId, String login);
    boolean existsByIdAndUserLogin(Long queryId, String login);
}
