package com.example.userservice.domain.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserRequestDto {
    @NotBlank(message = "Full name must not be blank")
    private String fullName;
    @NotBlank(message = "email must not be blank")
    private String email;
    @NotBlank(message = "email must not be blank")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
            message = "Password must contain at least one uppercase letter(A-Z),at least one lowercase letter(a-z),at least one digit (0-9) and length at least 8 characters")
    private String password;
    @NotBlank(message = "dateOfBirth must not be blank")
    @Pattern(regexp = "^(0[1-9]|[1-2][0-9]|3[0-1])\\.(0[1-9]|1[0-2])\\.\\d{4}$", message = "Invalid date format. Use dd.mm.yyyy.")
    private String dateOfBirth;
    @NotBlank(message = "Phone number must not be blank")
    private String phoneNumber;
    @NotBlank(message = "Gender must not be blank")
    private String gender;
}
