package com.example.userservice.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoleDto {
    @NotBlank(message = "Role name must not be blank")
    private String name;
    @NotEmpty(message = "Role permissions must not be empty")
    private List<String> permissions;
}
