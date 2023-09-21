package com.example.userservice.domain.dto.request.doctor;

import com.example.userservice.domain.dto.request.queue.QueueInformationRequestDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class DoctorQueueAndBookingData {
    private QueueInformationRequestDto queueInformationRequestDtoForBooking;;
    private QueueInformationRequestDto queueInformationRequestDtoForQueue;
}
