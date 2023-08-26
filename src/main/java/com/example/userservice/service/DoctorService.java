package com.example.userservice.service;

import com.example.userservice.domain.dto.request.DoctorCreateDto;
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
import com.example.userservice.repository.DoctorRepository;
import com.example.userservice.repository.PermissionRepository;
import com.example.userservice.repository.RoleRepository;
import com.example.userservice.repository.UserRepository;
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
import java.util.ArrayList;
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

    private final RestTemplate restTemplate;
    @Value("${services.create-time-slots}")
    private String createTimeSlots;


    public UserEntity saveDoctor(DoctorCreateDto drCreateDto, BindingResult bindingResult){
        if (bindingResult.hasErrors()) {
            List<ObjectError> errors = bindingResult.getAllErrors();
            throw new RequestValidationException(errors);
        }
        UserEntity user = userRepository.findByEmail(drCreateDto.getEmail()).orElseThrow(() -> new DataNotFoundException("User not found"));
        checkDoctorEmail(user);

        DoctorInfo doctorInfo = modelMapper.map(drCreateDto, DoctorInfo.class);
        doctorInfo.setDoctorSpecialty(DoctorSpecialty.valueOf(drCreateDto.getDoctorSpecialty()));
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

        return userRepository.save(user);
    }
    public List<UserEntity> getAllDoctor(int page,int size, UUID hospitalId){
        Sort sort = Sort.by(Sort.Direction.ASC, "fullName");
        Pageable pageable = PageRequest.of(page,size,sort);
        List<UserEntity> userEntities = userRepository.getAllDoctorsFromHospital(hospitalId, pageable).getContent();
        List<UserEntity> doctors = new ArrayList<>();
        for (UserEntity userEntity : userEntities) {
            if(userEntity.getDoctorInfo().getHospitalId().equals(hospitalId)){
                doctors.add(userEntity);
            }
        }
        return doctors;
    }
    public HttpStatus updateDoctorStatus(UUID drId, DoctorStatus status) {
        userRepository.getDoctorById(drId).orElseThrow(()-> new DataNotFoundException("Doctor not found"));
        doctorRepository.update(status, drId);
        return HttpStatus.OK;
    }

    public HttpStatus deleteDoctorFromHospital(UUID drId) {
        UserEntity user = userRepository.getDoctorById(drId).orElseThrow(() -> new DataNotFoundException("Doctor not found"));
        DoctorInfo doctorInfo = user.getDoctorInfo();
        doctorInfo.setHospitalId(null);
        user.setDoctorInfo(doctorInfo);
        userRepository.save(user);
        return HttpStatus.OK;
    }
    public List<RoleEntity> getRolesString(List<String> roles) {
        return roleRepository.findRoleEntitiesByNameIn(roles);
    }
    public List<PermissionEntity> getPermissionsString(List<String> permissions) {
        return permissionRepository.findPermissionEntitiesByPermissionIn(permissions);
    }
    public void checkDoctorEmail(UserEntity user) {
        boolean isPresent = false;
        for (RoleEntity role : user.getRoles()) {
            if (role.getName().equals("DOCTOR")) {
                isPresent = true;
                break;
            }
        }
        if (isPresent) {
            throw new UserBadRequestException("EMAIL ALREADY EXISTS");
        }
    }

    public ResponseEntity<String> setAvailability(DoctorAvailability doctorAvailability, Principal principal, BindingResult bindingResult) {
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
        ResponseEntity<String> response = restTemplate.exchange(
                URI.create(createTimeSlots),
                HttpMethod.POST,
                entity,
                String.class);
        return response;
    }
}