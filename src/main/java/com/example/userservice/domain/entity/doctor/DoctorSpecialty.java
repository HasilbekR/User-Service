package com.example.userservice.domain.entity.doctor;

import com.example.userservice.domain.entity.BaseEntity;
import jakarta.persistence.Entity;
import lombok.*;

@Entity(name = "doctor_specialty")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DoctorSpecialty extends BaseEntity {
    private String name;
    private String description;
    private String diseaseTreatment;
}
