package com.example.userservice.domain.dto.request.user;

import com.example.userservice.domain.entity.user.Gender;
import com.example.userservice.domain.entity.user.UserState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDetailsForFront {
    private UUID id;
    private String fullName;
    private String email;
    private UserState userState;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private Gender gender;
    private List<String> roles;
    private List<String> permissions;

}
