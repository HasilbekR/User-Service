package com.example.userservice.controller;

import com.example.userservice.domain.dto.request.DoctorCreateDto;
import com.example.userservice.domain.dto.request.UserDetailsRequestDto;
import com.example.userservice.domain.dto.request.UserRequestDto;
import com.example.userservice.domain.entity.doctor.DoctorAvailability;
import com.example.userservice.domain.entity.doctor.DoctorStatus;
import com.example.userservice.domain.entity.user.UserEntity;
import com.example.userservice.service.DoctorService;
import com.example.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final DoctorService doctorService;

    @GetMapping("/forgotten-password")
    public String forgottenPassword(
            @RequestParam String email
    ) {
        return userService.forgottenPassword(email);
    }

    @GetMapping("/{userId}/verify-code-for-update-password")
    public String verifyCodeForUpdatePassword(
            @PathVariable UUID userId,
            @RequestParam String code
    ) {
        return userService.verifyPasswordForUpdatePassword(userId, code);
    }


    @PutMapping("/{userId}/update-password")
    public String updatePassword(
            @PathVariable UUID userId,
            @RequestParam String confirmCode,
            @RequestParam String newPassword
    ) {
        return userService.updatePassword(userId, newPassword, confirmCode);
    }

    @DeleteMapping("/{userId}/delete-user")
    public String deleteUser(
            @PathVariable UUID userId
    ) {
        userService.deleteUser(userId);
        return "Successfully deleted";
    }

    @PutMapping("/{userId}/update-user")
    public UserEntity updateUpdateProfile(
            @PathVariable UUID userId,
            @RequestBody UserRequestDto update
    ) {
        return userService.updateProfile(userId, update);
    }

    @GetMapping("/get-all-user")
    public List<UserEntity> getAll(
            @RequestParam(required = false) int page,
            @RequestParam(required = false) int size
    ) {
        return userService.getAll(page, size);
    }

    @PostMapping("/getByEmail")
    public String getByEmail(
            @RequestBody UserDetailsRequestDto userDetailsRequestDto
    ) {
        UserEntity byEmail = userService.findByEmail(userDetailsRequestDto.getSource());

        return String.valueOf(byEmail.getId());
    }

    @GetMapping("/getById")
    public String getById(
            @RequestBody UserDetailsRequestDto userBookingDto
    ) {
        UserEntity user = userService.findById(UUID.fromString(userBookingDto.getSource()));
        return user.getEmail();
    }
    @PostMapping("/add-doctor")
    @PreAuthorize(value = "hasRole('ADMIN')")
    public ResponseEntity<UserEntity> addDoctor(
            @Valid @RequestBody DoctorCreateDto drCreateDto,
            BindingResult bindingResult
    ){
        return ResponseEntity.ok(doctorService.saveDoctor(drCreateDto,bindingResult));
    }
    @GetMapping("/get-all-doctors-hospital")
    public ResponseEntity<List<UserEntity>> getAll(
            @RequestParam(required = false,defaultValue = "0") int page,
            @RequestParam(required = false,defaultValue = "10") int size,
            @RequestParam UUID hospitalId
    ){
        return ResponseEntity.ok(doctorService.getAllDoctor(page,size, hospitalId));
    }

    @PutMapping("/change-doctor-status")
    @PreAuthorize(value = "hasAnyRole('DOCTOR','ADMIN')")
    public ResponseEntity<HttpStatus> changeStatus(
            @RequestParam UUID drId,
            @RequestParam String status
    ){
        return ResponseEntity.ok(doctorService.updateDoctorStatus(drId, DoctorStatus.valueOf(status)));
    }

    @DeleteMapping("/delete-doctor-from-hospital")
    @PreAuthorize(value = "hasRole('ADMIN')")
    public ResponseEntity<HttpStatus> delete(
            @RequestParam UUID doctorId
    ){
        return ResponseEntity.ok(doctorService.deleteDoctorFromHospital(doctorId));
    }

    @PostMapping("/set-doctor-availability")
    @PreAuthorize(value = "hasRole('DOCTOR')")
    public ResponseEntity<String>  setAvailability(
            @Valid @RequestBody DoctorAvailability doctorAvailability,
            Principal principal,
            BindingResult bindingResult
    ){
        return doctorService.setAvailability(doctorAvailability,principal,bindingResult);
    }

}
