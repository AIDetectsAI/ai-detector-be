package org.example.aidetectorbe.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class QueryController {
    
    @DeleteMapping(value = "/deleteQuery")
    public ResponseEntity<?> deleteQuery() {
        
    }
}
