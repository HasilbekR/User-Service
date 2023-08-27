package com.example.userservice.controller;

import com.example.userservice.domain.dto.request.DoctorCreateDto;
import com.example.userservice.domain.dto.request.ExchangeDataDto;
import com.example.userservice.domain.dto.request.user.UserRequestDto;
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
    @PostMapping("/add-doctor")
    @PreAuthorize(value = "hasRole('ADMIN') and hasAuthority('ADD_DOCTOR')")
    public ResponseEntity<UserEntity> addDoctor(
            @Valid @RequestBody DoctorCreateDto drCreateDto,
            BindingResult bindingResult,
            Principal principal
    ){
        return ResponseEntity.ok(doctorService.saveDoctor(drCreateDto,bindingResult,principal));
    }

    @PutMapping("/{userId}/update-user")
    public UserEntity updateUpdateProfile(
            @PathVariable UUID userId,
            @RequestBody UserRequestDto update
    ) {
        return userService.updateProfile(userId, update);
    }

    @GetMapping("/get-all-user")
    public ResponseEntity<List<UserEntity>> getAll(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(userService.getAll(page, size));
    }

    @PostMapping("/send-id")
    public String exchangeId(
            @RequestBody ExchangeDataDto exchangeDataDto
    ) {
        UserEntity user = userService.findByEmail(exchangeDataDto.getSource());

        return String.valueOf(user.getId());
    }
    @GetMapping("/get-me")
    public ResponseEntity<UserEntity> getMe(
            @RequestParam String email
    ){
        return ResponseEntity.ok(userService.findByEmail(email));
    }

    @PostMapping("/send-email")
    public String exchangeEmail(
            @RequestBody ExchangeDataDto userBookingDto
    ) {
        UserEntity user = userService.findById(UUID.fromString(userBookingDto.getSource()));
        return user.getEmail();
    }
    @GetMapping("/get-all-doctors-from-hospital")
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
            @RequestParam String email,
            @RequestParam String status
    ){
        return ResponseEntity.ok(doctorService.updateDoctorStatus(email, DoctorStatus.valueOf(status)));
    }

    @DeleteMapping("/delete-doctor-from-hospital")
    @PreAuthorize(value = "hasRole('SUPER_ADMIN')")
    public ResponseEntity<HttpStatus> delete(
            @RequestParam String email
    ){
        return ResponseEntity.ok(doctorService.deleteDoctorFromHospital(email));
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
