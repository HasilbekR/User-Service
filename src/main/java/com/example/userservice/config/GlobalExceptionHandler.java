package com.example.userservice.config;

import com.example.userservice.exception.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;

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
    @ExceptionHandler(value = {AccessDeniedException.class})
    public ResponseEntity<String> AccessDeciedExceptionHandler(
            AccessDeniedException e
    ){
        return ResponseEntity.status(403).body(e.getMessage());
    }

    @ExceptionHandler(value = {DataNotFoundException.class})
    public ResponseEntity<String> dataNotFoundExceptionHandler(
            DataNotFoundException e){
        return ResponseEntity.status(404).body(e.getMessage());
    }

    @ExceptionHandler(value = {UniqueObjectException.class})
    public ResponseEntity<String> uniqueObjectExceptionHandler(
            UniqueObjectException e){
        return ResponseEntity.status(401).body(e.getMessage());
    }
    @ExceptionHandler(value = {UserBadRequestException.class})
    public ResponseEntity<String> userBadRequestExceptionHandler(
            UserBadRequestException e){
        return ResponseEntity.status(400).body(e.getMessage());
    }
}
