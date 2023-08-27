package com.example.userservice.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.UUID;
@Data
public class DoctorCreateDto {
    @NotBlank(message = "Email must not be empty")
    private String email;
    @NotBlank(message = "Doctor's room must not be empty")
    private String roomNumber;
    @NotBlank(message = "Doctor's information must not be empty")
    private String info;
    @NotBlank(message = "Specialty must not be empty")
    private String doctorSpecialty;
    private List<String> roles;
    private List<String> permissions;
}
