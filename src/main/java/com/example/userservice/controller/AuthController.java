package com.example.userservice.controller;

import com.example.userservice.domain.dto.request.LoginRequestDto;
import com.example.userservice.domain.dto.request.UserRequestDto;
import com.example.userservice.domain.dto.response.JwtResponse;
import com.example.userservice.exception.RequestValidationException;
import com.example.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/user/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/sign-Up")
    public ResponseEntity<String> signUp(
            @Valid @RequestBody UserRequestDto userDto,
            BindingResult bindingResult
    ) throws RequestValidationException {
        if (bindingResult.hasErrors()){
            List<ObjectError> allErrors = bindingResult.getAllErrors();
            throw new RequestValidationException(allErrors);
        }
        return ResponseEntity.ok(userService.save(userDto));
    }

    @GetMapping("/sign-In")
    public ResponseEntity<JwtResponse> signIn(
            @RequestBody LoginRequestDto loginDto
    ){
        return ResponseEntity.ok(userService.signIn(loginDto));
    }

    @GetMapping("/access-token")
    public ResponseEntity<JwtResponse> getAccessToken(
            Principal principal
    ) {
        return ResponseEntity.ok(userService.getNewAccessToken(principal));
    }

    @GetMapping("/refresh-token")
    public ResponseEntity<JwtResponse> refreshAccessToken(
            Principal principal
    ){
        return ResponseEntity.ok(userService.getNewAccessToken(principal));
    }
}
