package com.example.userservice.service;

import com.example.userservice.domain.dto.request.DoctorCreateDto;
import com.example.userservice.domain.dto.response.StandardResponse;
import com.example.userservice.domain.dto.response.Status;
import com.example.userservice.domain.entity.doctor.DoctorAvailability;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
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

    private final RestTemplate restTemplate;
    @Value("${services.create-time-slots}")
    private String createTimeSlots;


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
    public StandardResponse<List<UserEntity>> getAllDoctor(int page,int size, UUID hospitalId){
        Sort sort = Sort.by(Sort.Direction.ASC,"fullName");
        Pageable pageable = PageRequest.of(page,size,sort);
        return StandardResponse.<List<UserEntity>>builder().status(Status.SUCCESS)
                .message("Doctor list "+page+"-page")
                .data(userRepository.getAllDoctorsFromHospital(hospitalId, pageable).getContent())
                .build();
    }
    public StandardResponse<String> updateDoctorStatus(String email, DoctorStatus status) {
        userRepository.getDoctorByEmail(email).orElseThrow(()-> new DataNotFoundException("Doctor not found"));
        doctorRepository.update(status, email);
        return StandardResponse.<String>builder().status(Status.SUCCESS).message("Doctor status updated").build();
    }
    public StandardResponse<List<String>> getDoctorSpecialtiesFromHospital(UUID hospitalId){
        return StandardResponse.<List<String>>builder().status(Status.SUCCESS)
                .message("List of specialties that exist in hospital")
                .data(userRepository.getAllSpecialtiesFromHospital(hospitalId))
                .build();
    }
    public StandardResponse<List<UserEntity>> getDoctorsBySpecialty(UUID hospitalId, String specialty){
        return StandardResponse.<List<UserEntity>>builder().status(Status.SUCCESS)
                .message("List of doctors by "+specialty+" specialty")
                .data(userRepository.getAllDoctorsBySpecialty(hospitalId, specialty))
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

    public StandardResponse<String> setAvailability(DoctorAvailability doctorAvailability, Principal principal, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new RequestValidationException(bindingResult.getAllErrors());
        }
        UserEntity doctorEntity = userRepository.findByEmail(principal.getName()).
                orElseThrow(() -> new DataNotFoundException("Doctor not found"));
        if (!doctorEntity.getId().equals(doctorAvailability.getDoctorId())) {
            throw new AccessDeniedException("Access denied");
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<DoctorAvailability> entity = new HttpEntity<>(doctorAvailability, httpHeaders);
        restTemplate.exchange(
                URI.create(createTimeSlots),
                HttpMethod.POST,
                entity,
                String.class);
        return StandardResponse.<String>builder().status(Status.SUCCESS).message("Time slots created for doctor "+doctorEntity.getFullName()+" for "+doctorAvailability.getDay()).build();
    }
}