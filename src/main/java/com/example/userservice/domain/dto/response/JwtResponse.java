package com.example.userservice.domain.dto.response;

import com.example.userservice.domain.dto.request.user.UserDetailsForFront;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private UserDetailsForFront user;
}
