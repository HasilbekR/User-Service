package com.example.userservice.domain.dto.request.user;

import lombok.Data;

@Data
public class VerifyCodeDto {
    private String email;
    private String code;
}
