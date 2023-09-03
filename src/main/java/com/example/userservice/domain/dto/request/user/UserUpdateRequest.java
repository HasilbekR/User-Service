package com.example.userservice.domain.dto.request.user;

import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserUpdateRequest {
    private String fullName;
    @Pattern(regexp = "^(0[1-9]|[1-2][0-9]|3[0-1])\\.(0[1-9]|1[0-2])\\.\\d{4}$", message = "Invalid date format. Use dd.mm.yyyy.")
    private String dateOfBirth;
    private String phoneNumber;
    private String gender;

}
