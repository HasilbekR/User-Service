package com.example.userservice.domain.dto.request.role;

import lombok.Data;

import java.util.UUID;

@Data
public class HospitalAssignDto {
    private String email;
    private UUID hospitalId;

}
