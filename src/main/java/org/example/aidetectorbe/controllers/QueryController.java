package org.example.aidetectorbe.controllers;

import org.example.aidetectorbe.logger.Log;
import org.example.aidetectorbe.services.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityNotFoundException;
import jakarta.websocket.server.PathParam;

import org.example.aidetectorbe.dto.ErrorResponse;

@RestController
@RequestMapping("/api")
public class QueryController {

    @Autowired
    private QueryService queryService;
    
    @DeleteMapping(value = "/users/{userId}/queries/{queryId}")
    public ResponseEntity<?> deleteQuery(@PathVariable Long queryId, @PathVariable Long userId) {
        Log.info("Received a request to remove a query: " + queryId + " from user: " + userId);
        try {
            queryService.deleteQuery(queryId, userId);
        } catch (EntityNotFoundException e) {
            Log.error("Didn't find the query to delete: " + e.getMessage());
            ErrorResponse errorResponse = new ErrorResponse("AI Service Error", e.getMessage(), 404);
            return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorResponse);
        }
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
    }
}
