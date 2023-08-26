package com.example.userservice.controller;

import com.example.userservice.domain.dto.request.DoctorCreateDto;
import com.example.userservice.domain.dto.request.LoginRequestDto;
import com.example.userservice.domain.dto.request.UserRequestDto;
import com.example.userservice.domain.dto.response.JwtResponse;
import com.example.userservice.domain.entity.user.UserEntity;
import com.example.userservice.exception.RequestValidationException;
import com.example.userservice.service.DoctorService;
import com.example.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/user/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final DoctorService doctorService;


    @PostMapping("/sign-up")
    public ResponseEntity<UserEntity> signUp(
            @Valid @RequestBody UserRequestDto userDto,
            BindingResult bindingResult
    ) throws RequestValidationException {
        if (bindingResult.hasErrors()){
            List<ObjectError> allErrors = bindingResult.getAllErrors();
            throw new RequestValidationException(allErrors);
        }
        return ResponseEntity.ok(userService.save(userDto));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<JwtResponse> signIn(
            @RequestBody LoginRequestDto loginDto
    ){
        return ResponseEntity.ok(userService.signIn(loginDto));
    }
    @GetMapping("/verify")
    public ResponseEntity<String> verify(
            @RequestParam String code,
            Principal principal
    ) {
        return ResponseEntity.ok(userService.verify(principal, code));
    }
    @GetMapping("/send-verification-code")
    public void sendVerificationCode(
            Principal principal
    ){
        userService.sendVerificationCode(principal.getName());
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

    @PostMapping("/add-doctor")
    @PreAuthorize(value = "hasRole('ADMIN')")
    public ResponseEntity<UserEntity> addDoctor(
            @Valid @RequestBody DoctorCreateDto drCreateDto,
            BindingResult bindingResult
    ){
        return ResponseEntity.ok(doctorService.saveDoctor(drCreateDto,bindingResult));
    }
}
