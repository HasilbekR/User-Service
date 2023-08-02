package com.example.userservice.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleDto {
    @NotBlank(message = "Role name must not be blank")
    private String name;
    @NotEmpty(message = "Role permissions must not be empty")
    private List<String> permissions;
}
