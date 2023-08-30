package com.example.userservice.config;

import com.example.userservice.domain.dto.response.StandardResponse;
import com.example.userservice.exception.*;
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
        return ResponseEntity.ok().body(StandardResponse.<String>builder().status("400").message(e.getMessage()).build());
    }

    @ExceptionHandler(value = {AuthenticationFailedException.class})
    public ResponseEntity<StandardResponse<String>> authenticationFailedExceptionHandler(
            AuthenticationFailedException e
    ){
        return ResponseEntity.ok().body(StandardResponse.<String>builder().status("401").message(e.getMessage()).build());

    }
    @ExceptionHandler(value = {AccessDeniedException.class})
    public StandardResponse<String> AccessDeniedExceptionHandler(
            AccessDeniedException e
    ){
        return StandardResponse.<String>builder().status("403").message(e.getMessage()).build();
    }

    @ExceptionHandler(value = {DataNotFoundException.class})
    public StandardResponse<String> dataNotFoundExceptionHandler(
            DataNotFoundException e){
        return StandardResponse.<String>builder().status("404").message(e.getMessage()).build();

    }

    @ExceptionHandler(value = {UniqueObjectException.class})
    public StandardResponse<String> uniqueObjectExceptionHandler(
            UniqueObjectException e){
        return StandardResponse.<String>builder().status("401").message(e.getMessage()).build();
    }
    @ExceptionHandler(value = {UserBadRequestException.class})
    public StandardResponse<String> userBadRequestExceptionHandler(
            UserBadRequestException e){
        return StandardResponse.<String>builder().status("400").message(e.getMessage()).build();
    }
}
