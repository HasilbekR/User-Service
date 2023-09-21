package com.example.userservice.domain.dto.request.user;

import lombok.*;

import java.time.LocalDate;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder

public class PatientRequestDto {
    private String name;
    private String gender;
    private LocalDate birthDate;
    private int queueNumber;
    private String appointmentDate;
    private String type;
}
