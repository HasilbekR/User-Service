package com.example.userservice.service;

import com.example.userservice.domain.dto.request.role.HospitalAssignDto;
import com.example.userservice.domain.dto.request.role.RoleAssignDto;
import com.example.userservice.domain.dto.request.role.RoleDto;
import com.example.userservice.domain.dto.request.ExchangeDataDto;
import com.example.userservice.domain.entity.role.PermissionEntity;
import com.example.userservice.domain.entity.role.RoleEntity;
import com.example.userservice.domain.entity.user.UserEntity;
import com.example.userservice.exception.DataNotFoundException;
import com.example.userservice.exception.UserBadRequestException;
import com.example.userservice.repository.PermissionRepository;
import com.example.userservice.repository.RoleRepository;
import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final RestTemplate restTemplate;
    private final JwtService jwtService;
    @Value("${services.get-hospital}")
    private String getHospitalId;

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

    public RoleEntity getRole(String name) {
        return roleRepository.findRoleEntityByName(name).orElseThrow(() -> new DataNotFoundException("Role not found"));
    }

    public RoleEntity update(RoleDto roleDto) {
        RoleEntity roleEntityByName = roleRepository.findRoleEntityByName(roleDto.getName()).orElseThrow(() -> new DataNotFoundException("Role not found"));

        if(roleDto.getPermissions() != null) {
            List<PermissionEntity> updatedPermissions = new ArrayList<>();
            for (String roleDtoPermission : roleDto.getPermissions()) {
                PermissionEntity permission = permissionRepository.findPermissionEntitiesByPermission(roleDtoPermission);
                if (permission != null) {
                    updatedPermissions.add(permission);
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

    public String assignRoleToUser(RoleAssignDto roleAssignDto, Principal principal) {
        if(Objects.equals(roleAssignDto.getRole(), "OWNER")) throw new AccessDeniedException("Unacceptable role name");
        RoleEntity roleEntity = roleRepository.findRoleEntityByName(roleAssignDto.getRole())
                .orElseThrow(() -> new DataNotFoundException("Role not found"));

        UserEntity user = userRepository.findByEmail(roleAssignDto.getEmail())
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        UserEntity userEntity = userRepository.findByEmail(principal.getName()).orElseThrow();

        List<RoleEntity> roles = user.getRoles();
        for (RoleEntity role : roles) {
            if(role.equals(roleEntity)) throw new UserBadRequestException("User already has "+role.getName()+" role");
        }
        List<String> permissions = roleAssignDto.getPermissions();
        List<PermissionEntity> permissionList = new ArrayList<>();
        for (String permission : permissions) {
            for (PermissionEntity roleEntityPermission : roleEntity.getPermissions()) {
                if(permission.equals(roleEntityPermission.getPermission())){
                    permissionList.add(roleEntityPermission);
                }
            }
        }
        roles.add(roleEntity);
        user.setRoles(roles);
        user.setPermissions(permissionList);
        user.setEmployeeOfHospital(userEntity.getEmployeeOfHospital());
        userRepository.save(user);
        return user.getEmail();
    }
    public void assignHospital(HospitalAssignDto hospitalAssignDto){
        UserEntity user = userRepository.findByEmail(hospitalAssignDto.getEmail())
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        UUID hospitalId = checkHospitalId(hospitalAssignDto.getHospitalId());
        if(hospitalId == null) throw new DataNotFoundException("Hospital not found");
        RoleEntity superAdmin = roleRepository.findRoleEntityByName("SUPER_ADMIN").orElseThrow();
        List<RoleEntity> roles = user.getRoles();
        for (RoleEntity role : roles) {
            if(role.equals(superAdmin)) throw new UserBadRequestException("User already has super_admin role");
        }
        roles.add(superAdmin);
        user.setRoles(roles);
        user.setEmployeeOfHospital(hospitalId);
        userRepository.save(user);
    }
    public UUID checkHospitalId(UUID id) {
        ExchangeDataDto exchangeDataDto = new ExchangeDataDto(id.toString());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("Authorization", "Bearer " + jwtService.generateAccessTokenForService("HOSPITAL-SERVICE"));
        HttpEntity<ExchangeDataDto> entity = new HttpEntity<>(exchangeDataDto, httpHeaders);
        ResponseEntity<String> response = restTemplate.exchange(
                URI.create(getHospitalId),
                HttpMethod.POST,
                entity,
                String.class);
        return UUID.fromString(Objects.requireNonNull(response.getBody()));
    }


}
