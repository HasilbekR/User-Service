package com.example.userservice.domain.dto.request.doctor;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class WorkingDays {
    private String weekDay;
    private LocalDate date;
}
