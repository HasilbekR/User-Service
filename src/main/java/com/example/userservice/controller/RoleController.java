package com.example.userservice.controller;

import com.example.userservice.domain.dto.request.role.DoctorSpecialtyDto;
import com.example.userservice.domain.dto.request.role.HospitalAssignDto;
import com.example.userservice.domain.dto.request.role.RoleAssignDto;
import com.example.userservice.domain.dto.request.role.RoleDto;
import com.example.userservice.domain.entity.doctor.DoctorSpecialty;
import com.example.userservice.domain.entity.role.RoleEntity;
import com.example.userservice.exception.RequestValidationException;
import com.example.userservice.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/user/role")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;
    @PostMapping("/create")
    @PreAuthorize(value = "hasRole('SUPER_ADMIN')")
    public ResponseEntity<RoleEntity> createRole(
            @Valid @RequestBody RoleDto roleDto,
            BindingResult bindingResult
            )throws RequestValidationException {
        if (bindingResult.hasErrors()){
            List<ObjectError> allErrors = bindingResult.getAllErrors();
            throw new RequestValidationException(allErrors);
        }
        return ResponseEntity.ok(roleService.save(roleDto));
    }

    @GetMapping("/get-role")
    @PreAuthorize(value = "hasRole('SUPER_ADMIN' or 'ADMIN')")
    public ResponseEntity<RoleEntity> getRole(
            @RequestParam String name
    ){
        return ResponseEntity.ok(roleService.getRole(name));
    }

    @PutMapping("/add-permissions-to-role")
    @PreAuthorize(value = "hasRole('SUPER_ADMIN')")
    public ResponseEntity<RoleEntity> update(
            @RequestBody RoleDto roleDto
    ){
        return ResponseEntity.ok(roleService.update(roleDto));
    }

    @PostMapping("/assign-role-permissions-to-user")
    @PreAuthorize(value = "hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> assignRoleToUser(
            @RequestBody RoleAssignDto roleAssignDto,
            Principal principal
    ) {
        return ResponseEntity.ok("Role successfully assigned to " + roleService.assignRoleToUser(roleAssignDto, principal));
    }

    @PostMapping("/add-permissions-to-user")
    @PreAuthorize(value = "hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> assignPermissionsToUser(
            @RequestBody RoleAssignDto roleAssignDto
    ) {
        return ResponseEntity.ok("Permissions successfully added to " + roleService.addPermissionsToUser(roleAssignDto));
    }

    @PostMapping("/assign-hospital")
    @PreAuthorize(value = "hasRole('OWNER')")
    public ResponseEntity<String> assignHospital(
            @RequestBody HospitalAssignDto hospitalAssignDto
            ){
        roleService.assignHospital(hospitalAssignDto);
        return ResponseEntity.ok("Successfully assigned hospital");
    }
    @PostMapping("/save-doctor-specialty")
    @PreAuthorize(value = "hasRole('ADMIN') and hasAuthority('CREATE_SPECIALTY')")
    public ResponseEntity<DoctorSpecialty> saveDoctorSpecialty(
            @RequestBody DoctorSpecialtyDto doctorSpecialtyDto
            ){
        return ResponseEntity.ok(roleService.saveDoctorSpecialty(doctorSpecialtyDto));
    }

}
