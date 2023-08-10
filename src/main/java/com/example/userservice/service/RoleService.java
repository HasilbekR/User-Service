package com.example.userservice.service;

import com.example.userservice.domain.dto.request.RoleDto;
import com.example.userservice.domain.entity.user.PermissionEntity;
import com.example.userservice.domain.entity.user.RoleEntity;
import com.example.userservice.domain.entity.user.UserEntity;
import com.example.userservice.exception.DataNotFoundException;
import com.example.userservice.exception.UniqueObjectException;
import com.example.userservice.repository.PermissionRepository;
import com.example.userservice.repository.RoleRepository;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;

    public RoleEntity save(RoleDto roleDto) {
        RoleEntity roleEntityByName = roleRepository.findRoleEntitiesByName(roleDto.getName());
        List<String> roleDtoPermissions = roleDto.getPermissions();
        // If role already exists
        if (roleEntityByName != null) {
            List<PermissionEntity> rolePermissions = roleEntityByName.getPermissions();
            // Checking if dto has another permission which role doesn't have
            for (String roleDtoPermission : roleDtoPermissions) {
                if (rolePermissions.stream().noneMatch(permission -> permission.getPermission().equals(roleDtoPermission))) {
                    // Checking if database has this extra permission
                    PermissionEntity permissionEntitiesByPermission = permissionRepository.findPermissionEntitiesByPermission(roleDtoPermission);
                    if (permissionEntitiesByPermission != null) {
                        rolePermissions.add(permissionEntitiesByPermission);
                    }
                    // If it doesn't have we create a new permission
                    else{
                        permissionEntitiesByPermission = PermissionEntity.builder().permission(roleDtoPermission).build();
                        rolePermissions.add(permissionRepository.save(permissionEntitiesByPermission));
                    }
                }
            }
            roleEntityByName.setPermissions(rolePermissions);
            return roleRepository.save(roleEntityByName);
        }
        // If role doesn't exists in db
        List<String> permissions = roleDto.getPermissions();
        List<PermissionEntity> rolePermission = new ArrayList<>();
        // Checking if db has its permissions
        for (String permission : permissions) {
            PermissionEntity permissionEntitiesByPermission = permissionRepository.findPermissionEntitiesByPermission(permission);
            // If not we create a new permission
            if (permissionEntitiesByPermission == null) {
                permissionEntitiesByPermission = PermissionEntity.builder().permission(permission).build();
                rolePermission.add(permissionRepository.save(permissionEntitiesByPermission));
            }else {
                rolePermission.add(permissionEntitiesByPermission);
            }
        }
        RoleEntity roleEntity = RoleEntity.builder().name(roleDto.getName()).permissions(rolePermission).build();
        return roleRepository.save(roleEntity);
    }

    public void delete(String name) {
        roleRepository.delete(roleRepository.findRoleEntityByName(name).orElseThrow(() -> new DataNotFoundException("Role not found")));
    }

    public RoleEntity getRole(String name) {
        return roleRepository.findRoleEntityByName(name).orElseThrow(() -> new DataNotFoundException("Role not found"));
    }

    public RoleEntity update(RoleDto roleDto, String name) {
        RoleEntity roleEntityByName = roleRepository.findRoleEntityByName(name).orElseThrow(() -> new DataNotFoundException("Role not found"));
        if (roleDto.getName() != null) {
            if (roleRepository.findRoleEntityByName(roleDto.getName()).isEmpty()) {
                roleEntityByName.setName(roleDto.getName());
            } else {
                throw new UniqueObjectException("Role already exists");
            }
        }
        if(roleDto.getPermissions() != null) {
            List<PermissionEntity> updatedPermissions = new ArrayList<>();
            for (String roleDtoPermission : roleDto.getPermissions()) {
                PermissionEntity permissionEntitiesByPermission = permissionRepository.findPermissionEntitiesByPermission(roleDtoPermission);
                if (permissionEntitiesByPermission != null) {
                    updatedPermissions.add(permissionEntitiesByPermission);
                } else {
                    PermissionEntity build = PermissionEntity.builder().permission(roleDtoPermission).build();
                    updatedPermissions.add(permissionRepository.save(build));
                }
            }
            roleEntityByName.setPermissions(updatedPermissions);
        }
        roleEntityByName.setUpdatedDate(LocalDateTime.now());
        return roleRepository.save(roleEntityByName);
    }

    public void assignRoleToUser(String roleName, UUID userId) {
        RoleEntity roleEntity = roleRepository.findRoleEntityByName(roleName)
                .orElseThrow(() -> new DataNotFoundException("Role not found"));

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        List<RoleEntity> roles = new ArrayList<>(); // Use a mutable collection
        roles.add(roleEntity);

        user.setRoles(roles);
        userRepository.save(user);
    }


}
