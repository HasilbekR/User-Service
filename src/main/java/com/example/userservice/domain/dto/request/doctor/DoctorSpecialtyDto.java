package com.example.userservice.domain.dto.request.doctor;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DoctorSpecialtyDto {
    @NotBlank(message = "Specialty name must not be blank")
    private String name;
    @NotBlank(message = "Specialty description must not be blank")
    private String description;
    @NotBlank(message = "Disease treatments must not be blank")
    private String diseaseTreatment;

}
