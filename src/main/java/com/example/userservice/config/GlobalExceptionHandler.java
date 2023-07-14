package com.example.userservice.config;

import com.example.userservice.exception.AuthenticationFailedException;
import com.example.userservice.exception.DataNotFoundException;
import com.example.userservice.exception.RequestValidationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {RequestValidationException.class})
    public ResponseEntity<String> requestValidationExceptionHandler(
            RequestValidationException e
    ){
        return ResponseEntity.status(400).body(e.getMessage());
    }

    @ExceptionHandler(value = {AuthenticationFailedException.class})
    public ResponseEntity<String> authenticationFailedExceptionHandler(
            AuthenticationFailedException e
    ){
        return ResponseEntity.status(401).body(e.getMessage());
    }

    @ExceptionHandler(value = {DataNotFoundException.class})
    public ResponseEntity<String> dataNotFoundExceptionHandler(
            DataNotFoundException e){
        return ResponseEntity.status(401).body(e.getMessage());
    }
}
