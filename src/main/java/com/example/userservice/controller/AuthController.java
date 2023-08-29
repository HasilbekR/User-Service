package com.example.userservice.controller;

import com.example.userservice.domain.dto.request.user.*;
import com.example.userservice.domain.dto.response.JwtResponse;
import com.example.userservice.domain.dto.response.StandardResponse;
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
@RequestMapping("/user/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/sign-up")
    public StandardResponse<JwtResponse> signUp(
            @Valid @RequestBody UserRequestDto userDto,
            BindingResult bindingResult
    ) throws RequestValidationException {
        if (bindingResult.hasErrors()){
            List<ObjectError> allErrors = bindingResult.getAllErrors();
            throw new RequestValidationException(allErrors);
        }
        return userService.save(userDto);
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
    @GetMapping("/forgot-password")
    public void forgottenPassword(
            @RequestParam String email
    ) {
        userService.forgottenPassword(email);
    }

    @PostMapping("/verify-code-for-update-password")
    public ResponseEntity<String> verifyCodeForUpdatePassword(
            @RequestBody VerifyCodeDto verifyCodeDto
    ) {
        return ResponseEntity.ok(userService.verifyPasswordForUpdatePassword(verifyCodeDto));
    }

    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword(
            @RequestBody UpdatePasswordDto updatePasswordDto
    ) {
        return ResponseEntity.ok(userService.updatePassword(updatePasswordDto));
    }
}
