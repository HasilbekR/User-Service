package com.example.userservice.domain.dto.request.queue;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class QueueInformationRequestDto {
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private UUID userId;
    private UUID doctorId;
    private Long queueNumber;
    private String queueStatus;
}
