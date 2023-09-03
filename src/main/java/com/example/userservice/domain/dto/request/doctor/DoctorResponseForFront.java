package com.example.userservice.domain.dto.request.doctor;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class DoctorResponseForFront {
    private UUID id;
    private String fullName;
    private String specialty;
    private String info;
    private List<WorkingDays> workingDays;
}
