package com.example.userservice.domain.dto.request.role;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoleAssignDto {
    private String role;
    private List<String> permissions;
    private String email;
}
