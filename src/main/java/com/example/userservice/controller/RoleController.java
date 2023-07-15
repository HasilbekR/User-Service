package com.example.userservice.controller;

import com.example.userservice.domain.dto.request.RoleDto;
import com.example.userservice.domain.entity.user.RoleEntity;
import com.example.userservice.exception.RequestValidationException;
import com.example.userservice.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/api/v1/role")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;
    @PostMapping("/create")
    public RoleEntity createRole(
            @Valid @RequestBody RoleDto roleDto,
            BindingResult bindingResult
            )throws RequestValidationException {
        if (bindingResult.hasErrors()){
            List<ObjectError> allErrors = bindingResult.getAllErrors();
            throw new RequestValidationException(allErrors);
        }
        return roleService.save(roleDto);
    }

    @DeleteMapping("/delete")
    public String deleteRole(
            @RequestParam String name
    ){
        roleService.delete(name);
        return "successfully deleted";
    }
    @GetMapping("/get-role")
    public RoleEntity getRole(
            @RequestParam String name
    ){
        return roleService.getRole(name);
    }

    @PutMapping("/update")
    public RoleEntity update(
            @RequestParam String name,
            @RequestBody RoleDto roleDto
    ){
        return roleService.update(roleDto, name);
    }
}
