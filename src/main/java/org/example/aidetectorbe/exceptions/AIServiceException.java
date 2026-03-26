package org.example.aidetectorbe.exceptions;

import lombok.Getter;

@Getter
public class AIServiceException extends Exception {
    
    private final int statusCode;

    public AIServiceException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 500;
    }
    
    public AIServiceException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

}