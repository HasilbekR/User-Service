package com.example.userservice.service;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.userservice.domain.dto.request.role.RoleDto;
import com.example.userservice.domain.dto.request.user.*;
import com.example.userservice.domain.dto.response.JwtResponse;
import com.example.userservice.domain.entity.VerificationEntity;
import com.example.userservice.domain.entity.role.PermissionEntity;
import com.example.userservice.domain.entity.role.RoleEntity;
import com.example.userservice.domain.entity.user.UserEntity;
import com.example.userservice.domain.entity.user.UserState;
import com.example.userservice.exception.UserBadRequestException;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.repository.VerificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private VerificationRepository verificationRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private MailService mailService;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSave() {
        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setEmail("user@example.com");
        userRequestDto.setPhoneNumber("1234567890");
        userRequestDto.setDateOfBirth("01.01.2000");
        userRequestDto.setPassword("password");
        userRequestDto.setGender("MALE");

        UserEntity userEntity = new UserEntity();
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setName("USER");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(roleService.save(any(RoleDto.class))).thenReturn(roleEntity);
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        UserDetailsForFront result = userService.save(userRequestDto);

        assertNotNull(result);
        assertEquals(UserState.UNVERIFIED, userEntity.getState());

        verify(userRepository, times(1)).findByEmail(userRequestDto.getEmail());
        verify(roleService, times(1)).save(any(RoleDto.class));
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }
    @Test
    public void testMappingUser() {
        UserEntity userEntity = new UserEntity();
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setName("USER");
        PermissionEntity permissionEntity = new PermissionEntity();
        permissionEntity.setPermission("GET");

        userEntity.getRoles().add(roleEntity);
        userEntity.getPermissions().add(permissionEntity);

        UserDetailsForFront userDetailsForFront = new UserDetailsForFront();
        userDetailsForFront.setRoles(new ArrayList<>());
        userDetailsForFront.setPermissions(new ArrayList<>());

        when(modelMapper.map(any(UserEntity.class), eq(UserDetailsForFront.class))).thenReturn(userDetailsForFront);

        UserDetailsForFront result = userService.mappingUser(userEntity);

        assertNotNull(result);
        assertEquals(1, result.getRoles().size());
        assertEquals(1, result.getPermissions().size());

        verify(modelMapper, times(1)).map(userEntity, UserDetailsForFront.class);
    }

    @Test
    public void testSignIn() {
        LoginRequestDto loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("user@example.com");
        loginRequestDto.setPassword("password");

        UserEntity userEntity = new UserEntity();
        userEntity.setPassword("hashedPassword");
        userEntity.setState(UserState.ACTIVE);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtService.generateAccessToken(any(UserEntity.class))).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any(UserEntity.class))).thenReturn("refreshToken");

        JwtResponse result = userService.signIn(loginRequestDto);

        assertNotNull(result);
        assertEquals("accessToken", result.getAccessToken());
        assertEquals("refreshToken", result.getRefreshToken());

        verify(userRepository, times(1)).findByEmail(loginRequestDto.getEmail());
    }
    @Test
    public void testUpdateProfile() {
        UUID userId = UUID.randomUUID();
        UserRequestDto update = new UserRequestDto();
        update.setFullName("Updated");

        UserEntity userEntity = new UserEntity();
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);

        UserEntity result = userService.updateProfile(userId, update);

        assertNotNull(result);
        assertEquals("Updated", result.getFullName());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(userEntity);
    }

    @Test
    public void testGetAll() {
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        List<UserEntity> userList = new ArrayList<>();
        when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(userList));

        List<UserEntity> result = userService.getAll(page, size);

        assertNotNull(result);
        assertEquals(userList, result);

        verify(userRepository, times(1)).findAll(pageable);
    }
    @Test
    public void testSendVerificationCode() {
        String email = "test@example.com";
        UserEntity userEntity = new UserEntity();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));
        userService.sendVerificationCode(email);
        verify(userRepository, times(1)).findByEmail(email);
        verify(verificationRepository, times(1)).save(any(VerificationEntity.class));
        verify(mailService, times(1)).sendVerificationCode(eq(email), anyString());
    }

    @Test
    public void testVerify() {
        Principal principal = () -> "test@example.com";
        String code = "123456";

        VerificationEntity verificationEntity = new VerificationEntity();
        verificationEntity.setCode(code);
        verificationEntity.setCreatedDate(LocalDateTime.now());

        when(verificationRepository.findByUserEmail(principal.getName())).thenReturn(Optional.of(verificationEntity));

        String result = userService.verify(principal, code);

        assertNotNull(result);
        assertEquals("Successfully Verified!", result);

        verify(verificationRepository, times(1)).findByUserEmail(principal.getName());
        verify(userRepository, times(1)).findByEmail(principal.getName());
        verify(userRepository, times(1)).save(any(UserEntity.class));
        verify(verificationRepository, times(1)).delete(verificationEntity);
    }

    @Test
    public void testForgottenPassword() {
        String email = "test@example.com";
        UserEntity userEntity = new UserEntity();
        VerificationEntity verificationEntity = new VerificationEntity();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));
        when(verificationRepository.findByUserEmail(email)).thenReturn(Optional.of(verificationEntity));

        userService.forgottenPassword(email);
        verify(userRepository, times(1)).findByEmail(email);
        verify(verificationRepository, times(1)).findByUserEmail(email);
        verify(verificationRepository, times(1)).delete(verificationEntity);
        verify(userService, times(1)).sendVerificationCode(email);
    }
    @Test
    public void testVerifyPasswordForUpdatePassword() {
        VerifyCodeDto verifyCodeDto = new VerifyCodeDto();
        verifyCodeDto.setEmail("test@example.com");
        verifyCodeDto.setCode("123456");

        VerificationEntity verificationEntity = new VerificationEntity();
        verificationEntity.setCode(verifyCodeDto.getCode());
        verificationEntity.setCreatedDate(LocalDateTime.now().minusMinutes(5));

        when(verificationRepository.findByUserEmail(verifyCodeDto.getEmail())).thenReturn(Optional.of(verificationEntity));

        String result = userService.verifyPasswordForUpdatePassword(verifyCodeDto);

        assertNotNull(result);
        assertEquals("Successfully verified", result);
        verify(verificationRepository, times(1)).findByUserEmail(verifyCodeDto.getEmail());
    }

    @Test
    public void testUpdatePassword() {
        UpdatePasswordDto updatePasswordDto = new UpdatePasswordDto();
        updatePasswordDto.setEmail("test@example.com");
        updatePasswordDto.setNewPassword("newPassword123");

        UserEntity userEntity = new UserEntity();
        when(userRepository.findByEmail(updatePasswordDto.getEmail())).thenReturn(Optional.of(userEntity));

        String result = userService.updatePassword(updatePasswordDto);

        assertNotNull(result);
        assertEquals("Successfully updated", result);

        verify(userRepository, times(1)).findByEmail(updatePasswordDto.getEmail());
        verify(userRepository, times(1)).save(userEntity);
    }
    @Test
    public void testGenerateVerificationCode() {
        String verificationCode = userService.generateVerificationCode();
        assertNotNull(verificationCode);
        assertTrue(verificationCode.matches("\\d+"));
    }
    @Test
    public void testCheckUserEmailAndPhoneNumber_EmailExists() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new UserEntity()));

        assertThrows(UserBadRequestException.class, () -> userService.checkUserEmailAndPhoneNumber(email, "1234567890"));

        verify(userRepository, times(1)).findByEmail(email);
        verify(userRepository, never()).findUserEntityByPhoneNumber(any());
    }

    @Test
    public void testCheckUserEmailAndPhoneNumber_PhoneNumberExists() {
        String phoneNumber = "1234567890";
        when(userRepository.findUserEntityByPhoneNumber(phoneNumber)).thenReturn(Optional.of(new UserEntity()));

        assertThrows(UserBadRequestException.class, () -> userService.checkUserEmailAndPhoneNumber("test@example.com", phoneNumber));

        verify(userRepository, never()).findByEmail(any());
        verify(userRepository, times(1)).findUserEntityByPhoneNumber(phoneNumber);
    }

    @Test
    public void testCheckUserEmailAndPhoneNumber_NoExistingData() {
        String email = "test@example.com";
        String phoneNumber = "1234567890";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.findUserEntityByPhoneNumber(phoneNumber)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> userService.checkUserEmailAndPhoneNumber(email, phoneNumber));

        verify(userRepository, times(1)).findByEmail(email);
        verify(userRepository, times(1)).findUserEntityByPhoneNumber(phoneNumber);
    }

    @Test
    public void testGetNewAccessToken() {
        String userEmail = "test@example.com";
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(userEmail);

        UserEntity userEntity = new UserEntity();
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(userEntity));
        when(jwtService.generateAccessToken(userEntity)).thenReturn("generatedAccessToken");

        JwtResponse result = userService.getNewAccessToken(principal);

        assertNotNull(result);
        assertEquals("generatedAccessToken", result.getAccessToken());

        verify(userRepository, times(1)).findByEmail(userEmail);
        verify(jwtService, times(1)).generateAccessToken(userEntity);
    }

    @Test
    public void testFindByEmail() {
        String userEmail = "test@example.com";
        UserEntity userEntity = new UserEntity();
        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(userEntity));

        UserEntity result = userService.findByEmail(userEmail);

        assertNotNull(result);
        assertSame(userEntity, result);

        verify(userRepository, times(1)).findByEmail(userEmail);
    }

    @Test
    public void testFindById() {
        UUID userId = UUID.randomUUID();
        UserEntity userEntity = new UserEntity();
        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        UserEntity result = userService.findById(userId);

        assertNotNull(result);
        assertSame(userEntity, result);

        verify(userRepository, times(1)).findById(userId);
    }


}
