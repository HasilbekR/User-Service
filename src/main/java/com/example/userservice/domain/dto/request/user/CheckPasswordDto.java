package com.example.userservice.domain.dto.request.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckPasswordDto {
    private String password;
}
