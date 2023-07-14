package com.example.userservice.controller;

import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    ){
        return userService.verify(userId,code);
    }
}
