package com.example.userservice.controller;

import com.example.userservice.domain.dto.request.DoctorCreateDto;
import com.example.userservice.domain.dto.request.ExchangeDataDto;
import com.example.userservice.domain.dto.request.doctor.DoctorDetailsForBooking;
import com.example.userservice.domain.dto.request.doctor.DoctorDetailsForFront;
import com.example.userservice.domain.dto.request.doctor.DoctorResponseForFront;
import com.example.userservice.domain.dto.request.doctor.DoctorsWithSpecialtiesForFront;
import com.example.userservice.domain.dto.request.user.*;
import com.example.userservice.domain.dto.response.StandardResponse;
import com.example.userservice.domain.entity.doctor.DoctorSpecialty;
import com.example.userservice.domain.entity.doctor.DoctorStatus;
import com.example.userservice.domain.entity.user.UserEntity;
import com.example.userservice.exception.RequestValidationException;
import com.example.userservice.service.DoctorService;
import com.example.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
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

    @PutMapping("/update-user")
    public StandardResponse<UserDetailsForFront> updateUpdateProfile(
            @Valid @RequestBody UserUpdateRequest update,
            BindingResult bindingResult,
            Principal principal
    )throws RequestValidationException {
        if (bindingResult.hasErrors()){
            List<ObjectError> allErrors = bindingResult.getAllErrors();
            throw new RequestValidationException(allErrors);
        }
        return userService.updateProfile(update, principal);
    }

    @GetMapping("/get-all-user")
    public StandardResponse<List<UserEntity>> getAll(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        return userService.getAll(page, size);
    }

    @PostMapping("/send-id")
    public UUID exchangeId(
            @RequestBody ExchangeDataDto exchangeDataDto
    ) {
        return userService.sendId(exchangeDataDto.getSource());
    }
    @PostMapping("/send-doctor")
    public DoctorDetailsForBooking exchangeFullName(
            @RequestBody ExchangeDataDto exchangeDataDto
    ) {
        return userService.sendDoctor(UUID.fromString(exchangeDataDto.getSource()));
    }

    @PostMapping("/send-email")
    public String exchangeEmail(
            @RequestBody ExchangeDataDto userBookingDto
    ) {
        return userService.sendEmail(UUID.fromString(userBookingDto.getSource()));
    }
    @PostMapping("/send-hospital-id")
    public UUID sendHospitalId(
            @RequestBody ExchangeDataDto exchangeDataDto
    ){
        return userService.sendHospitalId(exchangeDataDto.getSource());
    }

    @GetMapping("/get-me")
    public StandardResponse<UserDetailsForFront> getMe(
            Principal principal
    ){
        return userService.getMeByToken(principal.getName());
    }

    @GetMapping("/get-all-doctors-from-hospital")
    public StandardResponse<DoctorsWithSpecialtiesForFront> getAll(
            @RequestParam(required = false,defaultValue = "0") int page,
            @RequestParam(required = false,defaultValue = "10") int size,
            @RequestParam UUID hospitalId
    ){
        return doctorService.getAllDoctor(page,size, hospitalId);
    }
    @GetMapping("/get-doctors-by-specialty")
    public StandardResponse<List<DoctorDetailsForFront>> getDoctors(
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

    @GetMapping("/get-doctor-by-id")
    public StandardResponse<DoctorResponseForFront> getDoctorById(
            @RequestParam UUID doctorId
    ){
        return doctorService.getDoctorForFront(doctorId);
    }
    @GetMapping("/get-all-specialties")
    public StandardResponse<List<DoctorSpecialty>> getAllSpecialties(){
        return doctorService.getAllSpecialties();
    }
    @GetMapping("/get-specialty-by-id")
    public StandardResponse<DoctorSpecialty> getSpecialty(
            @RequestParam UUID specialtyId
    ){
        return doctorService.getSpecialty(specialtyId);
    }
    @PostMapping("/verify-code-for-changing-email")
    public StandardResponse<String> verifyCodeForChangingEmail(
            @RequestBody VerifyCodeDto verifyCodeDto,
            Principal principal
    ) {
        return userService.verifyCodeForChangingEmail(verifyCodeDto, principal);
    }
    @PostMapping("/check-password")
    public StandardResponse<Boolean> checkPassword(
            @RequestBody CheckPasswordDto checkPasswordDto,
            Principal principal
    ){
        return userService.checkPassword(checkPasswordDto, principal);
    }
    @GetMapping("/send-verification-for-changing-email")
    public StandardResponse<String> sendVerificationToChangeEmail(
            @RequestParam String email,
            Principal principal
    ){
        return userService.sendVerificationCodeToChangeEmail(email, principal);
    }

    @PostMapping("/countActiveDoctorBookingAndQueues")
    @PreAuthorize(value = "hasRole('DOCTOR') and hasRole('ADMIN')")
    public ResponseEntity<Long> getActiveBookingAndQueues(
            @RequestParam UUID doctorId
    ) {
        return ResponseEntity.ok(doctorService.countActiveDoctorBookingAndQueues(doctorId));
    }

    @PostMapping("/countCompleteDoctorBookingAndQueues")
    @PreAuthorize(value = "hasRole('DOCTOR') and hasRole('ADMIN')")
    public ResponseEntity<Long> getCompleteDoctorBookingAndQueues(
            @RequestParam UUID doctorId
    ) {
        return ResponseEntity.ok(doctorService.countCompleteDoctorBookingAndQueues(doctorId));
    }
//
//    @PostMapping("/getActiveDoctorBookingAndQueues")
//    public ResponseEntity<BookingRequestDto> getActiveDoctorBookingAndQueues(
//            @RequestParam UUID doctorId
//    ) {
//        return
//    }
//
//    @PostMapping("/getCompleteDoctorBookingAndQueues")
//    public ResponseEntity<BookingRequestDto> getCompleteDoctorBookingAndQueues(
//            @RequestParam UUID doctorId
//    ) {
//        return
//    }

}
