package com.example.userservice.controller;

import com.example.userservice.domain.dto.request.RoleDto;
import com.example.userservice.domain.entity.role.RoleEntity;
import com.example.userservice.domain.entity.user.UserEntity;
import com.example.userservice.exception.RequestValidationException;
import com.example.userservice.service.RoleService;
import com.example.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user/role")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;
    private final UserService userService;
    @PostMapping("/create")
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

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteRole(
            @RequestParam String name
    ){
        roleService.delete(name);
        return ResponseEntity.ok("successfully deleted");
    }
    @GetMapping("/get-role")
    public ResponseEntity<RoleEntity> getRole(
            @RequestParam String name
    ){
        return ResponseEntity.ok(roleService.getRole(name));
    }

    @PutMapping("/update")
    public ResponseEntity<RoleEntity> update(
            @RequestParam String name,
            @RequestBody RoleDto roleDto
    ){
        return ResponseEntity.ok(roleService.update(roleDto, name));
    }

    @PostMapping("/assign-role-to-user")
    public ResponseEntity<String> assignRoleToUser(
            @RequestParam String roleName,
            @RequestParam String email
    ) {
        UserEntity user = userService.findByEmail(email);
        roleService.assignRoleToUser(roleName, email);
        return ResponseEntity.ok("Role successfully assigned to " + user.getUsername());
    }
}
