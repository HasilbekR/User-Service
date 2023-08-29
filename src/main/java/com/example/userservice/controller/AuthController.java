package com.example.userservice.controller;

import com.example.userservice.domain.dto.request.user.*;
import com.example.userservice.domain.dto.response.JwtResponse;
import com.example.userservice.domain.dto.response.StandardResponse;
import com.example.userservice.exception.RequestValidationException;
import com.example.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public StandardResponse<JwtResponse> signIn(
            @RequestBody LoginRequestDto loginDto
    ){
        return userService.signIn(loginDto);
    }
    @GetMapping("/verify")
    public StandardResponse<String> verify(
            @RequestParam String code,
            Principal principal
    ) {
        return userService.verify(principal, code);
    }
    @GetMapping("/send-verification-code")
    public StandardResponse<String> sendVerificationCode(
            Principal principal
    ){
        return userService.sendVerificationCode(principal.getName());
    }
    @GetMapping("/access-token")
    public StandardResponse<JwtResponse> getAccessToken(
            Principal principal
    ) {
        return userService.getNewAccessToken(principal);
    }

    @GetMapping("/refresh-token")
    public StandardResponse<JwtResponse> refreshAccessToken(
            Principal principal
    ){
        return userService.getNewAccessToken(principal);
    }
    @GetMapping("/forgot-password")
    public StandardResponse<String> forgottenPassword(
            @RequestParam String email
    ) {
        return userService.forgottenPassword(email);
    }

    @PostMapping("/verify-code-for-update-password")
    public StandardResponse<String> verifyCodeForUpdatePassword(
            @RequestBody VerifyCodeDto verifyCodeDto
    ) {
        return userService.verifyPasswordForUpdatePassword(verifyCodeDto);
    }

    @PutMapping("/update-password")
    public StandardResponse<String> updatePassword(
            @RequestBody UpdatePasswordDto updatePasswordDto
    ) {
        return userService.updatePassword(updatePasswordDto);
    }
}
