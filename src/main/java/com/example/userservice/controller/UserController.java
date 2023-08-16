package com.example.userservice.controller;

import com.example.userservice.domain.dto.request.UserDetailsRequestDto;
import com.example.userservice.domain.dto.request.UserRequestDto;
import com.example.userservice.domain.entity.user.UserEntity;
import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/api/v1")
public class UserController {

    private final UserService userService;

    @GetMapping("/{userId}/verify")
    public String verify(
            @PathVariable UUID userId,
            @RequestParam String code
    ) {
        return userService.verify(userId, code);
    }

    @GetMapping("/forgotten-password")
    public String forgottenPassword(
            @RequestParam UUID userId
    ) {
        return userService.forgottenPassword(userId);
    }

    @GetMapping("/{userId}/verify-code-for-update-password")
    public String verifyCodeForUpdatePassword(
            @PathVariable UUID userId,
            @RequestParam String code
    ) {
        return userService.verifyPasswordForUpdatePassword(userId, code);
    }


    @PutMapping("/{userId}/update-password")
    public String updatePassword(
            @PathVariable UUID userId,
            @RequestParam String confirmCode,
            @RequestParam String newPassword
    ) {
        return userService.updatePassword(userId, newPassword, confirmCode);
    }

    @DeleteMapping("/{userId}/delete")
    public String deleteUser(
            @PathVariable UUID userId
    ) {
        userService.deleteUser(userId);
        return "Successfully deleted";
    }

    @PutMapping("/{userId}/update")
    public UserEntity updateUpdateProfile(
            @PathVariable UUID userId,
            @RequestBody UserRequestDto update
    ) {
        return userService.updateProfile(userId, update);
    }

    @GetMapping("/getAll")
    public List<UserEntity> getAll(
            @RequestParam(required = false) int page,
            @RequestParam(required = false) int size
    ) {
        return userService.getAll(page, size);
    }

    @PostMapping("/getByEmail")
    public String getByEmail(
            @RequestBody UserDetailsRequestDto userDetailsRequestDto
    ) {
        UserEntity byEmail = userService.findByEmail(userDetailsRequestDto.getSource());

        return String.valueOf(byEmail.getId());
    }

    @PostMapping("/getById")
    public String getById(
            @RequestBody UserDetailsRequestDto userBookingDto
    ) {
        UserEntity user = userService.findById(UUID.fromString(userBookingDto.getSource()));
        return user.getEmail();
    }
}
