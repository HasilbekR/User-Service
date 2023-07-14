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
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

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
                .link("http://localhost:8080/user/api/v1/"+ save.getId() +"/verify")
                .isActive(true)
                .build();
        verificationRepository.save(verificationEntity);
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

    public String verify(UUID userId,String code){
        VerificationEntity entity = verificationRepository.findByUserId(userId)
                .orElseThrow(() -> new DataNotFoundException("Verification code Not Found!"));

        if (code.equals(entity.getCode())){
            if (entity.getCreatedDate().plusMinutes(1).isAfter(LocalDateTime.now())){
                UserEntity user = userRepository.findById(userId)
                        .orElseThrow(() -> new DataNotFoundException("User Not Found"));
                user.setState(UserState.ACTIVE);
                userRepository.save(user);
                return "Successfully Verified!";
            }
            return "Verification Code has Expired!";
        }
        return "Wrong Verification Code!";
    }

    private String generateVerificationCode() {
        Random random = new Random(100000);
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
