package com.example.userservice.service;

import com.example.userservice.domain.dto.request.DoctorCreateDto;
import com.example.userservice.domain.dto.response.StandardResponse;
import com.example.userservice.domain.entity.doctor.DoctorAvailability;
import com.example.userservice.domain.entity.doctor.DoctorInfo;
import com.example.userservice.domain.entity.doctor.DoctorStatus;
import com.example.userservice.domain.entity.role.PermissionEntity;
import com.example.userservice.domain.entity.role.RoleEntity;
import com.example.userservice.domain.entity.user.UserEntity;
import com.example.userservice.exception.UserBadRequestException;
import com.example.userservice.repository.DoctorRepository;
import com.example.userservice.repository.PermissionRepository;
import com.example.userservice.repository.RoleRepository;
import com.example.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
@RunWith(MockitoJUnitRunner.class)
class DoctorServiceTest {

    @InjectMocks
    private DoctorService doctorService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSaveDoctor() {
        DoctorCreateDto doctorCreateDto = new DoctorCreateDto();

        BindingResult bindingResult = mock(BindingResult.class);
        Principal principal = mock(Principal.class);

        UserEntity existingUser = new UserEntity();
        existingUser.setEmail("existing@example.com");
        existingUser.setRoles(new ArrayList<>());
        existingUser.setPermissions(new ArrayList<>());

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(existingUser));

        when(roleRepository.findRoleEntitiesByNameIn(anyList())).thenReturn(new ArrayList<>());

        when(permissionRepository.findPermissionEntitiesByPermissionIn(anyList())).thenReturn(new ArrayList<>());

        StandardResponse<UserEntity> result = doctorService.saveDoctor(doctorCreateDto, bindingResult, principal);

        assertNotNull(result);
        verify(userRepository, times(1)).findByEmail(anyString());
        verify(doctorRepository, times(1)).save(any(DoctorInfo.class));
    }

    @Test
    public void testGetAllDoctor() {
        int page = 0;
        int size = 10;
        UUID hospitalId = UUID.randomUUID();

        when(userRepository.getAllDoctorsFromHospital(eq(hospitalId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(new ArrayList<>()));


        StandardResponse<List<UserEntity>> result = doctorService.getAllDoctor(page, size, hospitalId);

        assertNotNull(result);
        verify(userRepository, times(1)).getAllDoctorsFromHospital(any(UUID.class), any());
    }


    @Test
    public void testSetAvailability() {
        DoctorAvailability doctorAvailability = new DoctorAvailability();
        Principal principal = mock(Principal.class);
        BindingResult bindingResult = mock(BindingResult.class);

        UserEntity doctorEntity = new UserEntity();
        doctorEntity.setId(UUID.randomUUID());
        doctorEntity.setEmail("doctor@example.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(doctorEntity));

        ResponseEntity<String> mockResponse = new ResponseEntity<>("Success", HttpStatus.OK);
        when(restTemplate.exchange(any(), any(), any(), eq(String.class))).thenReturn(mockResponse);

        StandardResponse<String> response = doctorService.setAvailability(doctorAvailability, principal, bindingResult);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("Success", response.getMessage());

        verify(userRepository, times(1)).findByEmail(anyString());
        verify(restTemplate, times(1)).exchange(any(), any(), any(), eq(String.class));

    }
    @Test
    public void testUpdateDoctorStatus() {
        String email = "doctor@example.com";
        DoctorStatus status = DoctorStatus.ACTIVE;

        when(userRepository.getDoctorByEmail(email)).thenReturn(Optional.of(new UserEntity()));

        StandardResponse<String> result = doctorService.updateDoctorStatus(email, status);

        assertEquals(HttpStatus.OK, result);

        verify(userRepository, times(1)).getDoctorByEmail(email);
        verify(doctorRepository, times(1)).update(status, email);
    }

    @Test
    public void testDeleteDoctorFromHospital() {
        String email = "doctor@example.com";

        UserEntity userEntity = new UserEntity();
        DoctorInfo doctorInfo = new DoctorInfo();
        userEntity.setDoctorInfo(doctorInfo);

        when(userRepository.getDoctorByEmail(email)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        StandardResponse<String> result = doctorService.deleteDoctorFromHospital(email);

        assertEquals(HttpStatus.OK, result);

        verify(userRepository, times(1)).getDoctorByEmail(email);
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }
    @Test
    public void testGetRolesString() {
        List<String> roles = Arrays.asList("ROLE_DOCTOR", "ROLE_ADMIN");

        when(roleRepository.findRoleEntitiesByNameIn(roles)).thenReturn(new ArrayList<>());

        List<RoleEntity> result = doctorService.getRolesString(roles);

        assertNotNull(result);
        assertEquals(0, result.size());

        verify(roleRepository, times(1)).findRoleEntitiesByNameIn(roles);
    }

    @Test
    public void testGetPermissionsString() {
        List<String> permissions = Arrays.asList("PERMISSION_A", "PERMISSION_B");

        when(permissionRepository.findPermissionEntitiesByPermissionIn(permissions)).thenReturn(new ArrayList<>());

        List<PermissionEntity> result = doctorService.getPermissionsString(permissions);

        assertNotNull(result);
        assertEquals(0, result.size());

        verify(permissionRepository, times(1)).findPermissionEntitiesByPermissionIn(permissions);
    }

    @Test
    public void testCheckDoctorEmail() {
        UserEntity userEntity = new UserEntity();
        RoleEntity doctorRole = new RoleEntity();
        doctorRole.setName("DOCTOR");
        userEntity.getRoles().add(doctorRole);
        assertThrows(UserBadRequestException.class, () -> doctorService.checkDoctorEmail(userEntity));
    }
}
