package com.example.userservice.service;

import com.example.userservice.domain.dto.request.LoginRequestDto;
import com.example.userservice.domain.dto.request.UserRequestDto;
import com.example.userservice.domain.dto.response.JwtResponse;
import com.example.userservice.domain.entity.VerificationEntity;
import com.example.userservice.domain.entity.user.PermissionEntity;
import com.example.userservice.domain.entity.user.RoleEntity;
import com.example.userservice.domain.entity.user.UserEntity;
import com.example.userservice.domain.entity.user.UserState;
import com.example.userservice.exception.AuthenticationFailedException;
import com.example.userservice.exception.DataNotFoundException;
import com.example.userservice.exception.UserBadRequestException;
import com.example.userservice.repository.PermissionRepository;
import com.example.userservice.repository.RoleRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.repository.VerificationRepository;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final VerificationRepository verificationRepository;
    private final ModelMapper modelMapper;
    private final MailService mailService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;


    public String save(UserRequestDto userRequestDto) {
        checkUserEmail(userRequestDto.getEmail());

        UserEntity userEntity = modelMapper.map(userRequestDto, UserEntity.class);
        userEntity.setState(UserState.UNVERIFIED);
        userEntity.setRoles(getRolesFromStrings(userRequestDto.getRoles()));
        userEntity.setPermissions(getPermissionsFromStrings(userRequestDto.getPermissions()));
        userEntity.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        UserEntity save = userRepository.save(userEntity);
        VerificationEntity verificationEntity = VerificationEntity.builder()
                .userId(save)
                .code(generateVerificationCode())
                .link("http://localhost:8081/"+ save.getId() +"/verify")
                .isActive(true)
                .build();
        return mailService.sendVerificationCode(userEntity.getEmail(), verificationEntity.getCode(), verificationEntity.getLink());
    }


    public JwtResponse signIn(LoginRequestDto loginRequestDto){
        UserEntity userEntity = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new DataNotFoundException("Incorrect email or password"));
        if(passwordEncoder.matches(loginRequestDto.getPassword(),userEntity.getPassword())){
            String accessToken = jwtService.generateAccessToken(userEntity);
            String refreshToken = jwtService.generateRefreshToken(userEntity);
            return JwtResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();
        }
        throw new AuthenticationFailedException("Incorrect username or password");
    }

    public void deleteUser(UUID userId){
        if (userRepository.deleteUserEntityById(userId).isEmpty()){
            throw new UserBadRequestException("user not found");
        }
    }

    public UserEntity updateProfile(UUID userId, UserRequestDto update){
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserBadRequestException("user not found"));
        modelMapper.map(update,userEntity);
        return userRepository.save(userEntity);
    }


    public List<UserEntity> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findAll(pageable).getContent();
    }

    public String verify(UUID userId,String code){
        VerificationEntity entity = verificationRepository.findByUserId(userId)
                .orElseThrow(() -> new DataNotFoundException("Verification code Not Found!"));

        if (code.equals(entity.getCode())){
            if (entity.getCreatedDate().plusMinutes(1).isBefore(LocalDateTime.now())){
                UserEntity user = userRepository.findById(userId)
                        .orElseThrow(() -> new DataNotFoundException("User Not Found"));
                user.setState(UserState.ACTIVE);
                entity.setActive(false);
                userRepository.save(user);
                return "Successfully Verified!";
            }
            return "Verification Code has Expired!";
        }
        return "Wrong Verification Code!";
    }

    public String forgottenPassword(UUID userId) {
        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        VerificationEntity verificationEntity = VerificationEntity.builder()
                .userId(userEntity)
                .code(generateVerificationCode())
                .link("http://localhost:8080/user/api/v1/"+ userEntity.getId() +"/verify-code-for-update-password")
                .isActive(true)
                .build();
        verificationRepository.save(verificationEntity);

        return mailService.sendConfirmationCodeForUpdatePassword(userEntity.getEmail(), verificationEntity.getCode(), verificationEntity.getLink());
    }

    public String verifyPasswordForUpdatePassword (UUID userId,String code){
        VerificationEntity entity = verificationRepository.findUserEntityByisActive(userId)
                .orElseThrow(() -> new DataNotFoundException("Verification code Not Found!"));

        UserEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("User not found!"));


            if (code.equals(entity.getCode())) {
                if (entity.getCreatedDate().plusMinutes(2).isAfter(LocalDateTime.now())) {
                    UserEntity user = userRepository.findById(userId)
                            .orElseThrow(() -> new DataNotFoundException("User Not Found"));
                    userRepository.save(user);
                    VerificationEntity verificationEntity = VerificationEntity.builder()
                            .userId(userEntity)
                            .code(generateVerificationCode())
                            .link("http://localhost:8080/user/api/v1/" + userEntity.getId() + "/update-password")
                            .isActive(true)
                            .build();
                    return mailService.sendConfirmationCodeForUpdatePassword(userEntity.getEmail(), verificationEntity.getCode(), verificationEntity.getLink());
                }
                return "Verification Code has Expired!";
            }
            return "Wrong Verification Code!";
    }


    public String updatePassword(UUID userId, String newPassword, String confirmPassword) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new DataNotFoundException("This user not found"));

        if (Objects.equals(newPassword, confirmPassword)) {
            user.setPassword(passwordEncoder.encode(newPassword));
            return "Password successfully changed";
        }
        return "the password did not match";
    }


    private String generateVerificationCode() {
        Random random = new Random(System.currentTimeMillis());
        return String.valueOf(random.nextInt(1000000));
    }


    private void checkUserEmail(String email) {
        if(userRepository.findByEmail(email).isPresent()) {
            throw new UserBadRequestException("email already exists");
        }
    }

    private List<PermissionEntity> getPermissionsFromStrings(List<String> permissions) {
        return permissionRepository.findPermissionEntitiesByPermissionIn(permissions);
    }

    private List<RoleEntity> getRolesFromStrings(List<String> roles) {
        return roleRepository.findRoleEntitiesByNameIn(roles);
    }

    public JwtResponse getNewAccessToken(Principal principal) {
        UserEntity userEntity = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new DataNotFoundException("user not found"));
        String accessToken = jwtService.generateAccessToken(userEntity);
        return JwtResponse.builder().accessToken(accessToken).build();
    }
}
