package com.example.userservice.domain.dto.request.doctor;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DoctorsWithSpecialtiesForFront {
    private List<DoctorDetailsForFront> doctors;
    private List<String> specialties;
}
