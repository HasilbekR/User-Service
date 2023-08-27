package com.example.userservice.domain.dto.request;

import com.example.userservice.domain.entity.role.PermissionEntity;
import com.example.userservice.domain.entity.role.RoleEntity;
import com.example.userservice.domain.entity.user.UserState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserDetailsForFront {
    private UUID id;
    private String fullName;
    private UserState userState;
    private List<RoleEntity> roles;
    private List<PermissionEntity> permissions;

}
