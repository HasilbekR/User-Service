package com.example.userservice.domain.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class StandardResponse<T> {
    private String status;
    private String message;
    private T data;
}