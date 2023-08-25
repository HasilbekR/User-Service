package com.example.userservice.domain.entity.doctor;

import com.example.userservice.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity(name = "doctors_info")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DoctorInfo extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private DoctorStatus status;
    @Column(nullable = false)
    private String roomNumber;
    private UUID hospitalId;
    @Enumerated(EnumType.STRING)
    private DoctorSpecialty doctorSpecialty;
}