package com.example.userservice.domain.dto.request.user;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CheckPasswordDto {
    private String password;
}
