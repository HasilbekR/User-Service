package com.example.userservice.domain.dto.request.doctor;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class DoctorDetailsForBooking {
    private UUID id;
    private String fullName;
    private String roomNumber;
    private String specialty;
    private String gender;
    private UUID hospitalId;
}
