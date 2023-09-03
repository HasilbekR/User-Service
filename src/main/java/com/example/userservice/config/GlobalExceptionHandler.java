package com.example.userservice.config;

import com.example.userservice.domain.dto.response.StandardResponse;
import com.example.userservice.domain.dto.response.Status;
import com.example.userservice.exception.*;
import jakarta.ws.rs.InternalServerErrorException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {RequestValidationException.class})
    public ResponseEntity<StandardResponse<String>>
            requestValidationExceptionHandler(
            RequestValidationException e
    ){
        return ResponseEntity.status(400).body(StandardResponse.<String>builder().status(Status.ERROR).message(e.getMessage()).build());
    }

    @ExceptionHandler(value = {AuthenticationFailedException.class})
    public ResponseEntity<StandardResponse<String>> authenticationFailedExceptionHandler(
            AuthenticationFailedException e
    ){
        return ResponseEntity.status(401).body(StandardResponse.<String>builder().status(Status.ERROR).message(e.getMessage()).build());
    }
    @ExceptionHandler(value = {AccessDeniedException.class})
    public ResponseEntity<StandardResponse<String>> AccessDeniedExceptionHandler(
            AccessDeniedException e
    ){
        return ResponseEntity.status(403).body(StandardResponse.<String>builder().status(Status.ERROR).message(e.getMessage()).build());
    }

    @ExceptionHandler(value = {DataNotFoundException.class})
    public ResponseEntity<StandardResponse<String>> dataNotFoundExceptionHandler(
            DataNotFoundException e){
        return ResponseEntity.status(404).body(StandardResponse.<String>builder().status(Status.ERROR).message(e.getMessage()).build());

    }

    @ExceptionHandler(value = {UniqueObjectException.class})
    public ResponseEntity<StandardResponse<String>> uniqueObjectExceptionHandler(
            UniqueObjectException e){
        return ResponseEntity.status(401).body(StandardResponse.<String>builder().status(Status.ERROR).message(e.getMessage()).build());
    }
    @ExceptionHandler(value = {UserBadRequestException.class})
    public ResponseEntity<StandardResponse<String>> userBadRequestExceptionHandler(
            UserBadRequestException e){
        return ResponseEntity.status(400).body(StandardResponse.<String>builder().status(Status.ERROR).message(e.getMessage()).build());
    }
    @ExceptionHandler(value = {InternalServerErrorException.class})
    public ResponseEntity<StandardResponse<String>> internalServerExceptionHandler(
    ){
        return ResponseEntity.status(401).body(StandardResponse.<String>builder().status(Status.ERROR).message("Data not exist").build());

    }
}
