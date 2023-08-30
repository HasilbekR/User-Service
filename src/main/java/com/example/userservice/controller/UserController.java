package com.example.userservice.controller;

import com.example.userservice.domain.dto.request.DoctorCreateDto;
import com.example.userservice.domain.dto.request.ExchangeDataDto;
import com.example.userservice.domain.dto.request.user.UserRequestDto;
import com.example.userservice.domain.dto.response.StandardResponse;
import com.example.userservice.domain.entity.doctor.DoctorAvailability;
import com.example.userservice.domain.entity.doctor.DoctorStatus;
import com.example.userservice.domain.entity.user.UserEntity;
import com.example.userservice.service.DoctorService;
import com.example.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public StandardResponse<UserEntity> addDoctor(
            @Valid @RequestBody DoctorCreateDto drCreateDto,
            BindingResult bindingResult,
            Principal principal
    ){
        return doctorService.saveDoctor(drCreateDto,bindingResult,principal);
    }

    @PutMapping("/{userId}/update-user")
    public StandardResponse<UserEntity> updateUpdateProfile(
            @PathVariable UUID userId,
            @RequestBody UserRequestDto update
    ) {
        return userService.updateProfile(userId, update);
    }

    @GetMapping("/get-all-user")
    public StandardResponse<List<UserEntity>> getAll(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        return userService.getAll(page, size);
    }

    @PostMapping("/send-id")
    public String exchangeId(
            @RequestBody ExchangeDataDto exchangeDataDto
    ) {
        return userService.sendId(exchangeDataDto.getSource());
    }

    @PostMapping("/send-email")
    public String exchangeEmail(
            @RequestBody ExchangeDataDto userBookingDto
    ) {
        return userService.sendEmail(UUID.fromString(userBookingDto.getSource()));
    }

    @GetMapping("/get-me")
    public StandardResponse<UserEntity> getMe(
            @RequestParam String email
    ){
        return userService.getMeByEmail(email);
    }

    @GetMapping("/get-all-doctors-from-hospital")
    public StandardResponse<List<UserEntity>> getAll(
            @RequestParam(required = false,defaultValue = "0") int page,
            @RequestParam(required = false,defaultValue = "10") int size,
            @RequestParam UUID hospitalId
    ){
        return doctorService.getAllDoctor(page,size, hospitalId);
    }
    @GetMapping("/get-doctor-specialties")
    public StandardResponse<List<String>> getSpecialties(
            @RequestParam UUID hospitalId
    ){
        return doctorService.getDoctorSpecialtiesFromHospital(hospitalId);
    }
    @GetMapping("/get-doctors-by-specialty")
    public StandardResponse<List<UserEntity>> getDoctors(
            @RequestParam String specialty,
            @RequestParam UUID hospitalId
    ){
        return doctorService.getDoctorsBySpecialty(hospitalId, specialty);
    }

    @PutMapping("/change-doctor-status")
    @PreAuthorize(value = "hasAnyRole('DOCTOR','ADMIN')")
    public StandardResponse<String> changeStatus(
            @RequestParam String email,
            @RequestParam String status
    ){
        return doctorService.updateDoctorStatus(email, DoctorStatus.valueOf(status));
    }

    @DeleteMapping("/delete-doctor-from-hospital")
    @PreAuthorize(value = "hasRole('SUPER_ADMIN')")
    public StandardResponse<String> delete(
            @RequestParam String email
    ){
        return doctorService.deleteDoctorFromHospital(email);
    }

    @PostMapping("/set-doctor-availability")
    @PreAuthorize(value = "hasRole('DOCTOR')")
    public StandardResponse<String>  setAvailability(
            @Valid @RequestBody DoctorAvailability doctorAvailability,
            Principal principal,
            BindingResult bindingResult
    ){
        return doctorService.setAvailability(doctorAvailability,principal,bindingResult);
    }
}
