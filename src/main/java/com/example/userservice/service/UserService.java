package com.example.userservice.service;

import com.example.userservice.domain.dto.request.*;
import com.example.userservice.domain.dto.response.JwtResponse;
import com.example.userservice.domain.entity.VerificationEntity;
import com.example.userservice.domain.entity.role.PermissionEntity;
import com.example.userservice.domain.entity.role.RoleEntity;
import com.example.userservice.domain.entity.user.Gender;
import com.example.userservice.domain.entity.user.UserEntity;
import com.example.userservice.domain.entity.user.UserState;
import com.example.userservice.exception.AuthenticationFailedException;
import com.example.userservice.exception.DataNotFoundException;
import com.example.userservice.exception.UserBadRequestException;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.repository.VerificationRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final VerificationRepository verificationRepository;
    private final ModelMapper modelMapper;
    private final MailService mailService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;


    public UserDetailsForFront save(UserRequestDto userRequestDto) {
        checkUserEmailAndPhoneNumber(userRequestDto.getEmail(), userRequestDto.getPhoneNumber());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate dateOfBirth = LocalDate.parse(userRequestDto.getDateOfBirth(), formatter);

        UserEntity userEntity = modelMapper.map(userRequestDto, UserEntity.class);
        userEntity.setState(UserState.UNVERIFIED);
        userEntity.setDateOfBirth(dateOfBirth);
        userEntity.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        RoleDto roleDto = RoleDto.builder().name("USER").permissions(List.of("GET", "UPDATE","DELETE")).build();
        RoleEntity roleEntity = roleService.save(roleDto);
        userEntity.setRoles(List.of(roleEntity));
        userEntity.setPermissions(List.of(roleEntity.getPermissions().toArray(new PermissionEntity[0])));
        if(!(Objects.equals(userRequestDto.getGender(), "MALE") || Objects.equals(userRequestDto.getGender(), "FEMALE"))){
            throw new DataNotFoundException("Gender not found");
        }
        userEntity.setGender(Gender.valueOf(userRequestDto.getGender()));
        UserEntity user = userRepository.save(userEntity);
        return mappingUser(user);
    }
    private UserDetailsForFront mappingUser(UserEntity userEntity){
        UserDetailsForFront map = modelMapper.map(userEntity, UserDetailsForFront.class);
        List<String> roles = new ArrayList<>();
        for (RoleEntity role : userEntity.getRoles()) {
            roles.add(role.getName());
        }
        List<String> permissions = new ArrayList<>();
        for (PermissionEntity permission : userEntity.getPermissions()) {
            permissions.add(permission.getPermission());
        }
        map.setRoles(roles);
        map.setPermissions(permissions);
        return map;
    }


    public JwtResponse signIn(LoginRequestDto loginRequestDto) {
        UserEntity userEntity = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new DataNotFoundException("Incorrect email or password"));
        if (userEntity.getState() == UserState.BLOCKED) {
            throw new AuthenticationFailedException("Your account is blocked. Please contact to admin@gmail.com for further information");
        }
        if (passwordEncoder.matches(loginRequestDto.getPassword(), userEntity.getPassword())) {
            String accessToken = jwtService.generateAccessToken(userEntity);
            String refreshToken = jwtService.generateRefreshToken(userEntity);
            UserDetailsForFront user = mappingUser(userEntity);
            return JwtResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(user)
                    .build();
        }
        throw new AuthenticationFailedException("Incorrect username or password");
    }

    public UserEntity updateProfile(UUID userId, UserRequestDto update) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserBadRequestException("user not found"));
        modelMapper.map(update, userEntity);
        userEntity.setUpdatedDate(LocalDateTime.now());
        return userRepository.save(userEntity);
    }

    public void deleteUser(UUID userId) {
        if (userRepository.findById(userId).isEmpty()) {
            throw new DataNotFoundException("user not found!");
        }
        userRepository.deleteById(userId);
    }


    public List<UserEntity> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable).getContent();
    }
    public void sendVerificationCode(String email){
        UserEntity userEntity = userRepository.findByEmail(email).orElseThrow(() -> new DataNotFoundException("User not found"));
        VerificationEntity verificationEntity = VerificationEntity.builder()
                .userId(userEntity)
                .code(generateVerificationCode())
                .isActive(true)
                .build();
        verificationRepository.save(verificationEntity);
        mailService.sendVerificationCode(userEntity.getEmail(), verificationEntity.getCode(), verificationEntity.getLink());
    }

    public String verify(Principal principal, String code) {
        VerificationEntity entity = verificationRepository.findByUserEmail(principal.getName())
                .orElseThrow(() -> new DataNotFoundException("Verification code Not Found!"));

        if (code.equals(entity.getCode()) && entity.isActive()) {
            if (entity.getCreatedDate().plusMinutes(10).isAfter(LocalDateTime.now())) {
                UserEntity user = userRepository.findByEmail(principal.getName())
                        .orElseThrow(() -> new DataNotFoundException("User Not Found"));
                user.setState(UserState.ACTIVE);
                entity.setActive(false);
                userRepository.save(user);
                verificationRepository.delete(entity);
                return "Successfully Verified!";
            }
            verificationRepository.delete(entity);
            return "Verification code has expired!";
        }
        return "Wrong Verification Code!";
    }

    public void forgottenPassword(UserDetailsRequestDto email) {
        userRepository.findByEmail(email.getSource())
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        Optional<VerificationEntity> byUserEmail = verificationRepository.findByUserEmail(email.getSource());
        if(byUserEmail.isPresent()){
            verificationRepository.delete(byUserEmail.orElseThrow());
        }
        sendVerificationCode(email.getSource());
    }

    public String verifyPasswordForUpdatePassword(UUID userId, String code) {
        VerificationEntity entity = verificationRepository.findUserEntityByisActive(userId)
                .orElseThrow(() -> new DataNotFoundException("Verification code doesn't exists or expired"));

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found!"));


        if (code.equals(entity.getCode())) {
            if (entity.getCreatedDate().plusMinutes(10).isAfter(LocalDateTime.now())) {
                VerificationEntity verificationEntity = VerificationEntity.builder()
                        .userId(userEntity)
                        .code(generateVerificationCode())
                        .link("http://localhost:8082/user/api/v1/" + userEntity.getId() + "/update-password")
                        .isActive(true)
                        .build();
                VerificationEntity expired = verificationRepository.findUserEntityByisActive(userId)
                        .orElseThrow(() -> new DataNotFoundException("Code not found"));
                expired.setActive(false);
                verificationRepository.save(expired);
                verificationRepository.save(verificationEntity);
                return mailService.sendConfirmationCodeForUpdatePassword(userEntity.getEmail(), verificationEntity.getCode(), verificationEntity.getLink());
            }
            entity.setActive(false);
            verificationRepository.save(entity);
            return "Verification Code has Expired!";
        }
        return "Wrong Verification Code!";
    }

    public String updatePassword(UUID userId, String newPassword, String confirmCode) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("This user not found"));

        VerificationEntity verification = verificationRepository.findUserEntityByisActive(userId)
                .orElseThrow(() -> new DataNotFoundException("Verification code expired or not found"));

        if (verification.getCreatedDate().plusMinutes(10).isAfter(LocalDateTime.now())) {
            if (Objects.equals(verification.getCode(), confirmCode)) {
                String encoded = passwordEncoder.encode(newPassword);
                user.setPassword(encoded);
                userRepository.save(user);
                verification.setActive(false);
                verificationRepository.save(verification);
                return "Success";
            }
            return "Wrong Verification Code";
        }
        verification.setActive(false);
        verificationRepository.save(verification);
        return "Verification Code Expired";
    }

    private String generateVerificationCode() {
        Random random = new Random(System.currentTimeMillis());
        return String.valueOf(random.nextInt(1000000));
    }

    private void checkUserEmailAndPhoneNumber(String email, String phoneNumber) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserBadRequestException("email already exists");
        }
        if (userRepository.findUserEntityByPhoneNumber(phoneNumber).isPresent()) {
            throw new UserBadRequestException("phone number already exists");
        }
    }

    public JwtResponse getNewAccessToken(Principal principal) {
        UserEntity userEntity = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new DataNotFoundException("user not found"));
        String accessToken = jwtService.generateAccessToken(userEntity);
        return JwtResponse.builder().accessToken(accessToken).build();
    }
    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new DataNotFoundException("User not found"));
    }
    public UserEntity findById(UUID userId){
        return userRepository.findById(userId).orElseThrow(() -> new DataNotFoundException("User not found"));
    }
    public UserEntity findDocById(UUID userId){
        return userRepository.getDoctorById(userId).orElseThrow(() -> new DataNotFoundException("User not found"));
    }
}
