package com.example.userservice.service;

import com.example.userservice.domain.dto.request.DoctorCreateDto;
import com.example.userservice.domain.dto.request.ExchangeDataDto;
import com.example.userservice.domain.dto.request.doctor.DoctorDetailsForFront;
import com.example.userservice.domain.dto.request.doctor.DoctorResponseForFront;
import com.example.userservice.domain.dto.request.doctor.DoctorsWithSpecialtiesForFront;
import com.example.userservice.domain.dto.request.doctor.WorkingDays;
import com.example.userservice.domain.dto.response.StandardResponse;
import com.example.userservice.domain.dto.response.Status;
import com.example.userservice.domain.entity.doctor.DoctorInfo;
import com.example.userservice.domain.entity.doctor.DoctorSpecialty;
import com.example.userservice.domain.entity.doctor.DoctorStatus;
import com.example.userservice.domain.entity.role.PermissionEntity;
import com.example.userservice.domain.entity.role.RoleEntity;
import com.example.userservice.domain.entity.user.UserEntity;
import com.example.userservice.exception.DataNotFoundException;
import com.example.userservice.exception.RequestValidationException;
import com.example.userservice.exception.UserBadRequestException;
import com.example.userservice.repository.*;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DoctorService {
    private final ModelMapper modelMapper;
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final DoctorSpecialtyRepository doctorSpecialtyRepository;
    private final JwtService jwtService;

    private final RestTemplate restTemplate;
    @Value("${services.count-doctors-queues-by-doctorId-and-queueStatus-active}")
    private String countDoctorQueuesByDoctorIdAndQueueStatusActive;
    @Value("${services.count-doctors-queues-by-doctorId-and-queueStatus-complete}")
    private String countDoctorQueuesByDoctorIdAndQueueStatusComplete;
    @Value("${services.count-doctors-bookings-by-doctorId-and-queueStatus-active}")
    private String countDoctorBookingsByDoctorIdAndQueueStatusActive;
    @Value("${services.count-doctors-bookings-by-doctorId-and-queueStatus-complete}")
    private String countDoctorBookingsByDoctorIdAndQueueStatusComplete;
    @Value("${services.get-working-days}")
    private String getWorkingDays;


    public StandardResponse<UserEntity> saveDoctor(DoctorCreateDto drCreateDto, BindingResult bindingResult, Principal principal){
        if (bindingResult.hasErrors()) {
            List<ObjectError> errors = bindingResult.getAllErrors();
            throw new RequestValidationException(errors);
        }
        UserEntity user = userRepository.findByEmail(drCreateDto.getEmail()).orElseThrow(() -> new DataNotFoundException("User not found"));
        checkDoctorEmail(user);

        DoctorInfo doctorInfo = modelMapper.map(drCreateDto, DoctorInfo.class);
        DoctorSpecialty specialty = doctorSpecialtyRepository.findDoctorSpecialtyByName(drCreateDto.getDoctorSpecialty()).orElseThrow(() -> new DataNotFoundException("Doctor specialty not found"));
        doctorInfo.setDoctorSpecialty(specialty);
        List<RoleEntity> roles = user.getRoles();
        roles.addAll(getRolesString(drCreateDto.getRoles()));
        user.setRoles(roles);
        List<PermissionEntity> permissions = user.getPermissions();
        permissions.addAll(getPermissionsString(drCreateDto.getPermissions()));
        user.setPermissions(permissions);
        doctorInfo.setStatus(DoctorStatus.ACTIVE);
        doctorInfo.setCreatedDate(LocalDateTime.now());
        doctorInfo.setUpdatedDate(LocalDateTime.now());
        user.setDoctorInfo(doctorRepository.save(doctorInfo));

        UserEntity userEntity = userRepository.findByEmail(principal.getName()).orElseThrow();
        user.setEmployeeOfHospital(userEntity.getEmployeeOfHospital());

        return StandardResponse.<UserEntity>builder().status(Status.SUCCESS)
                .message("Doctor successfully added")
                .data(userRepository.save(user))
                .build();
    }
    public StandardResponse<DoctorsWithSpecialtiesForFront> getAllDoctor(int page,int size, UUID hospitalId){
        Sort sort = Sort.by(Sort.Direction.ASC,"fullName");
        Pageable pageable = PageRequest.of(page,size,sort);
        List<UserEntity> doctors = userRepository.getAllDoctorsFromHospital(hospitalId, pageable).getContent();
        DoctorsWithSpecialtiesForFront doctorsWithSpecialtiesForFront = DoctorsWithSpecialtiesForFront.builder()
                .doctors(mapDoctor(doctors))
                .specialties(getDoctorSpecialtiesFromHospital(hospitalId)).build();
        return StandardResponse.<DoctorsWithSpecialtiesForFront>builder().status(Status.SUCCESS)
                .message("Doctor list "+page+"-page")
                .data(doctorsWithSpecialtiesForFront)
                .build();
    }
    public List<DoctorDetailsForFront> mapDoctor(List<UserEntity> doctors){
        List<DoctorDetailsForFront> doctorDetailsForFronts = new ArrayList<>();
        for (UserEntity doctor : doctors) {
            doctorDetailsForFronts.add(DoctorDetailsForFront.builder()
                    .id(doctor.getId())
                    .fullName(doctor.getFullName())
                    .specialty(doctor.getDoctorInfo().getDoctorSpecialty().getName())
                    .build());
        }
        return doctorDetailsForFronts;
    }
    public StandardResponse<String> updateDoctorStatus(String email, DoctorStatus status) {
        userRepository.getDoctorByEmail(email).orElseThrow(()-> new DataNotFoundException("Doctor not found"));
        doctorRepository.update(status, email);
        return StandardResponse.<String>builder().status(Status.SUCCESS).message("Doctor status updated").build();
    }
    public List<String> getDoctorSpecialtiesFromHospital(UUID hospitalId){
        return userRepository.getAllSpecialtiesFromHospital(hospitalId);
    }
    public StandardResponse<List<DoctorSpecialty>> getAllSpecialties(){
        return StandardResponse.<List<DoctorSpecialty>>builder().status(Status.SUCCESS)
                .message("All specialties").data(doctorSpecialtyRepository.findAll()).build();
    }
    public StandardResponse<List<DoctorDetailsForFront>> getDoctorsBySpecialty(UUID hospitalId, String specialty){
        List<UserEntity> doctors = userRepository.getAllDoctorsBySpecialty(hospitalId, specialty);
        return StandardResponse.<List<DoctorDetailsForFront>>builder().status(Status.SUCCESS)
                .message("List of doctors by "+specialty+" specialty")
                .data(mapDoctor(doctors))
                .build();
    }

    public StandardResponse<String> deleteDoctorFromHospital(String email) {
        UserEntity user = userRepository.getDoctorByEmail(email).orElseThrow(() -> new DataNotFoundException("Doctor not found"));
        user.setEmployeeOfHospital(null);
        userRepository.save(user);
        return StandardResponse.<String>builder().status(Status.SUCCESS).message("Doctor has been fired from hospital").build();
    }
    public List<RoleEntity> getRolesString(List<String> roles) {
        return roleRepository.findRoleEntitiesByNameIn(roles);
    }
    public List<PermissionEntity> getPermissionsString(List<String> permissions) {
        return permissionRepository.findPermissionEntitiesByPermissionIn(permissions);
    }
    public void checkDoctorEmail(UserEntity user) {
        for (RoleEntity role : user.getRoles()) {
            if (role.getName().equals("DOCTOR")) {
                throw new UserBadRequestException("EMAIL ALREADY EXISTS");
            }
        }
    }
    public List<WorkingDays> getWorkingDaysOfDoctor(UUID doctorId){
        ExchangeDataDto exchangeDataDto = new ExchangeDataDto(doctorId.toString());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.set("Authorization", "Bearer " + jwtService.generateAccessTokenForService("HYBRID-BOOKING-SERVICE"));
        HttpEntity<ExchangeDataDto> entity = new HttpEntity<>(exchangeDataDto, httpHeaders);
        ParameterizedTypeReference<List<LocalDate>> responseType = new ParameterizedTypeReference<>() {};
        List<LocalDate> dates = restTemplate.exchange(
                URI.create(getWorkingDays),
                HttpMethod.POST,
                entity,
                responseType).getBody();

        List<WorkingDays> workingDays = new ArrayList<>();
        assert dates != null;
        for (LocalDate date : dates) {
            workingDays.add(WorkingDays.builder().weekDay(date.getDayOfWeek().toString()).date(date).build());
        }
        return workingDays;
    }

    public StandardResponse<DoctorResponseForFront> getDoctorForFront(UUID doctorId) {
        UserEntity doctor = userRepository.getDoctorById(doctorId).orElseThrow(() -> new DataNotFoundException("Doctor not found"));
        return StandardResponse.<DoctorResponseForFront>builder()
                .status(Status.SUCCESS)
                .message("Doctor info")
                .data(DoctorResponseForFront.builder()
                        .id(doctorId)
                        .fullName(doctor.getFullName())
                        .specialty(doctor.getDoctorInfo().getDoctorSpecialty().getName())
                        .info(doctor.getDoctorInfo().getInfo())
                        .workingDays(getWorkingDaysOfDoctor(doctorId))
                        .build())
                .build();
    }

    public StandardResponse<DoctorSpecialty> getSpecialty(UUID specialtyId) {
        return StandardResponse.<DoctorSpecialty>builder().status(Status.SUCCESS).message("Specialty info").data(doctorSpecialtyRepository.findById(specialtyId).orElseThrow()).build();
    }

    public Long countActiveDoctorBookingAndQueues(UUID doctorId) {
        ExchangeDataDto exchangeData = new ExchangeDataDto(String.valueOf(doctorId));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ExchangeDataDto> entity = new HttpEntity<>(exchangeData, httpHeaders);
        ResponseEntity<Long> queueResponse = restTemplate.exchange(
                URI.create(countDoctorQueuesByDoctorIdAndQueueStatusActive),
                HttpMethod.POST,
                entity,
                Long.class);
        ResponseEntity<Long> bookingResponse = restTemplate.exchange(
                URI.create(countDoctorBookingsByDoctorIdAndQueueStatusActive),
                HttpMethod.POST,
                entity,
                Long.class);
        System.out.println(Objects.requireNonNull(queueResponse.getBody()));
        System.out.println(Objects.requireNonNull(bookingResponse.getBody()));
        return queueResponse.getBody() + bookingResponse.getBody();
    }


    public Long countCompleteDoctorBookingAndQueues(UUID doctorId) {
        ExchangeDataDto exchangeData = new ExchangeDataDto(String.valueOf(doctorId));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ExchangeDataDto> entity = new HttpEntity<>(exchangeData, httpHeaders);
        ResponseEntity<Long> queueResponse = restTemplate.exchange(
                URI.create(countDoctorQueuesByDoctorIdAndQueueStatusComplete),
                HttpMethod.POST,
                entity,
                Long.class);
        ResponseEntity<Long> bookingResponse = restTemplate.exchange(
                URI.create(countDoctorBookingsByDoctorIdAndQueueStatusComplete),
                HttpMethod.POST,
                entity,
                Long.class);
        System.out.println(Objects.requireNonNull(queueResponse.getBody()));
        System.out.println(Objects.requireNonNull(bookingResponse.getBody()));
        return queueResponse.getBody() + bookingResponse.getBody();
    }
}