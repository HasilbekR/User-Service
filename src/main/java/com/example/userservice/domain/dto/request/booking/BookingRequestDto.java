package com.example.userservice.domain.dto.request.booking;

import com.example.userservice.domain.entity.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class BookingRequestDto extends BaseEntity {
    private String patientName;
}
