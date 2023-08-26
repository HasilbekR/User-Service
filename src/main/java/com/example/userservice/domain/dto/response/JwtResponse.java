package com.example.userservice.domain.dto.response;

import com.example.userservice.domain.entity.user.UserEntity;
import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private UserEntity userEntity;
}
