package com.example.userservice.domain.dto.request.role;

import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "Role name must not be blank")
    private String name;
    @NotBlank(message = "Permissions must not be blank")
    private List<String> permissions;
    @NotBlank(message = "Email must not be blank")
    private String email;
}
