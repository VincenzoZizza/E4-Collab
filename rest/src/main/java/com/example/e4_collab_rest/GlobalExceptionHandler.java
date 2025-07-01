package com.example.e4_collab_rest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
//@Profile("backend")
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
        Map<String, String> errorResponse = new HashMap<>();

        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());

        errorResponse.put("status", String.valueOf(status.value()));
        errorResponse.put("error", status.getReasonPhrase());

        String reason = ex.getReason();
        if(reason != null) {
            errorResponse.put("reason", reason);
        }

        return ResponseEntity.status(ex.getStatusCode()).body(errorResponse);
    }
}
