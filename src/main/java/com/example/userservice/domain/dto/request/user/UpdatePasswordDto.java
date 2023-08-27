package com.example.userservice.domain.dto.request.user;

import lombok.Data;

@Data
public class UpdatePasswordDto {
    private String email;
    private String newPassword;
}
