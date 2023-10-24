package com.example.userservice.domain.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class SetEmployment {
    private String email;
    private UUID hospitalId;
}
