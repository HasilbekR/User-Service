package com.example.userservice.service;

import com.example.userservice.domain.dto.request.SetEmployment;
import com.example.userservice.domain.dto.request.doctor.DoctorDetailsForBooking;
import com.example.userservice.domain.dto.request.role.RoleDto;
import com.example.userservice.domain.dto.request.user.*;
import com.example.userservice.domain.dto.response.JwtResponse;
import com.example.userservice.domain.dto.response.StandardResponse;
import com.example.userservice.domain.dto.response.Status;
import com.example.userservice.domain.entity.VerificationEntity;
import com.example.userservice.domain.entity.role.PermissionEntity;
import com.example.userservice.domain.entity.role.RoleEntity;
import com.example.userservice.domain.entity.user.Gender;
import com.example.userservice.domain.entity.user.UserEntity;
import com.example.userservice.domain.entity.user.UserState;
import com.example.userservice.exception.AuthenticationFailedException;
import com.example.userservice.exception.DataNotFoundException;
import com.example.userservice.exception.UniqueObjectException;
import com.example.userservice.exception.UserBadRequestException;
import com.example.userservice.repository.RoleRepository;
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
    private final RoleRepository roleRepository;


    public StandardResponse<JwtResponse> save(UserRequestDto userRequestDto) {
        checkUserEmailAndPhoneNumber(userRequestDto.getEmail(), userRequestDto.getPhoneNumber());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate dateOfBirth = LocalDate.parse(userRequestDto.getDateOfBirth(), formatter);

        UserEntity userEntity = modelMapper.map(userRequestDto, UserEntity.class);
        userEntity.setState(UserState.UNVERIFIED);
        userEntity.setDateOfBirth(dateOfBirth);
        userEntity.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        RoleEntity role = roleRepository.findRoleEntitiesByName("USER");
        if (role == null) {
            RoleDto roleDto = RoleDto.builder().name("USER").permissions(List.of("GET", "UPDATE", "DELETE")).build();
            role = roleService.save(roleDto).getData();
        }
        userEntity.setRoles(List.of(role));
        userEntity.setPermissions(List.of(role.getPermissions().toArray(new PermissionEntity[0])));
        if(!(Objects.equals(userRequestDto.getGender(), "MALE") || Objects.equals(userRequestDto.getGender(), "FEMALE"))){
            throw new DataNotFoundException("Gender not found");
        }
        userEntity.setGender(Gender.valueOf(userRequestDto.getGender()));
        userEntity = userRepository.save(userEntity);
        String accessToken = jwtService.generateAccessToken(userEntity);
        String refreshToken = jwtService.generateRefreshToken(userEntity);
        UserDetailsForFront user = mappingUser(userEntity);
        JwtResponse jwtResponse = JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(user)
                .build();
        return StandardResponse.<JwtResponse> builder()
                .status(Status.SUCCESS)
                .message("Successfully signed up")
                .data(jwtResponse).build();
    }
    public UserDetailsForFront mappingUser(UserEntity userEntity){
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


    public StandardResponse<JwtResponse> signIn(LoginRequestDto loginRequestDto) {
        UserEntity userEntity = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new DataNotFoundException("Incorrect email or password"));
        if (userEntity.getState() == UserState.BLOCKED) {
            throw new AuthenticationFailedException("Your account is blocked. Please contact to admin@gmail.com for further information");
        }
        if (passwordEncoder.matches(loginRequestDto.getPassword(), userEntity.getPassword())) {
            String accessToken = jwtService.generateAccessToken(userEntity);
            String refreshToken = jwtService.generateRefreshToken(userEntity);
            List<RoleEntity> roles = userEntity.getRoles();
            if(!roles.isEmpty()){
                int position = 0;
                int i = 0;
                for(RoleEntity role : roles){
                    if(role.getName().equals("ADMIN")){
                        position = i;
                    }
                    i++;
                }
                if(position > 0){
                    RoleEntity roleEntity = roles.get(0);
                    RoleEntity admin = roles.get(position);
                    roles.set(0, admin);
                    roles.set(position, roleEntity);
                }
            }
            UserDetailsForFront user = mappingUser(userEntity);
            JwtResponse jwtResponse = JwtResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .user(user)
                    .build();
            return StandardResponse.<JwtResponse>builder().status(Status.SUCCESS).message("Successfully signed in").data(jwtResponse).build();
        }else {
            throw new AuthenticationFailedException("Incorrect username or password");
        }
    }

    public StandardResponse<UserDetailsForFront> updateProfile(UserUpdateRequest update, Principal principal) {
        UserEntity userEntity = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new UserBadRequestException("user not found"));
        modelMapper.map(update, userEntity);
        userEntity.setUpdatedDate(LocalDateTime.now());
        if(update.getDateOfBirth() != null){
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate dateOfBirth = LocalDate.parse(update.getDateOfBirth(), formatter);
            userEntity.setDateOfBirth(dateOfBirth);
        }
        if(update.getGender() != null){
            if(!(Objects.equals(update.getGender(), "MALE") || Objects.equals(update.getGender(), "FEMALE"))){
                throw new DataNotFoundException("Gender not found");
            }
        }
        userRepository.save(userEntity);

        return StandardResponse.<UserDetailsForFront>builder().status(Status.SUCCESS)
                .message("User updated successfully")
                .data(mappingUser(userEntity))
                .build();
    }

    public StandardResponse<List<UserEntity>> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return StandardResponse.<List<UserEntity>>builder().status(Status.SUCCESS)
                .message("User list "+page+"-page")
                .data(userRepository.findAll(pageable).getContent())
                .build();
    }
    public StandardResponse<String> sendVerificationCode(String email){
        UserEntity userEntity = userRepository.findByEmail(email).orElseThrow(() -> new DataNotFoundException("User not found"));
        sendVerification(userEntity, email);
        return StandardResponse.<String>builder().status(Status.SUCCESS).message("Verification code has been sent").build();
    }
    public StandardResponse<String> sendVerificationCodeToChangeEmail(String email, Principal principal){
        UserEntity user = userRepository.findByEmail(principal.getName()).orElseThrow(() -> new DataNotFoundException("User not found"));
        Optional<UserEntity> userEntity = userRepository.findByEmail(email);
        if(userEntity.isPresent()) throw new UniqueObjectException("Email already exists");
        sendVerification(user, email);
        return StandardResponse.<String>builder().status(Status.SUCCESS).message("Verification code has been sent").build();
    }
    public void sendVerification(UserEntity userEntity, String email){
        if(verificationRepository.findByUserEmail(userEntity.getEmail()).isPresent()){
            verificationRepository.delete(verificationRepository.findByUserEmail(userEntity.getEmail()).orElseThrow());
        }
        VerificationEntity verificationEntity = VerificationEntity.builder()
                .userId(userEntity)
                .code(generateVerificationCode())
                .build();
        verificationRepository.save(verificationEntity);
        mailService.sendVerificationCode(email, verificationEntity.getCode());
    }

    public StandardResponse<String> verify(Principal principal, String code) {
        VerificationEntity entity = verificationRepository.findByUserEmail(principal.getName())
                .orElseThrow(() -> new DataNotFoundException("Verification code Not Found!"));

        if (code.equals(entity.getCode())) {
            if (entity.getCreatedDate().plusMinutes(10).isAfter(LocalDateTime.now())) {
                UserEntity user = userRepository.findByEmail(principal.getName())
                        .orElseThrow(() -> new DataNotFoundException("User Not Found"));
                user.setState(UserState.ACTIVE);
                userRepository.save(user);
                verificationRepository.delete(entity);
                return StandardResponse.<String>builder().status(Status.SUCCESS).message("Successfully Verified!").build();
            }
            verificationRepository.delete(entity);
            throw new UserBadRequestException("Verification Code has Expired!");
        }
        throw new UserBadRequestException("Wrong Verification Code!");
    }

    public StandardResponse<String> forgottenPassword(String  email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new DataNotFoundException("User not found"));
        Optional<VerificationEntity> byUserEmail = verificationRepository.findByUserEmail(email);
        if(byUserEmail.isPresent()){
            verificationRepository.delete(byUserEmail.orElseThrow());
        }
        sendVerificationCode(email);
        return StandardResponse.<String>builder().status(Status.SUCCESS).message("Verification code has been sent").build();
    }

    public StandardResponse<String> verifyPasswordForUpdatePassword(VerifyCodeDto verifyCodeDto) {
        checkVerificationCode(verifyCodeDto);
        return StandardResponse.<String>builder().status(Status.SUCCESS).message("Successfully verified").build();
    }
    public StandardResponse<String> verifyCodeForChangingEmail(VerifyCodeDto verifyCodeDto, Principal principal) {
        String newEmail = verifyCodeDto.getEmail();
        verifyCodeDto.setEmail(principal.getName());
        checkVerificationCode(verifyCodeDto);
        UserEntity userEntity = userRepository.findByEmail(verifyCodeDto.getEmail()).orElseThrow(() -> new DataNotFoundException("User not found"));
        userEntity.setEmail(newEmail);
        userRepository.save(userEntity);
        return StandardResponse.<String>builder().status(Status.SUCCESS).message("Email successfully changed").build();
    }

    public void checkVerificationCode(VerifyCodeDto verifyCodeDto) {
        VerificationEntity entity = verificationRepository.findByUserEmail(verifyCodeDto.getEmail())
                .orElseThrow(() -> new DataNotFoundException("Verification code doesn't exists or expired"));
        if (verifyCodeDto.getCode().equals(entity.getCode())) {
            if (entity.getCreatedDate().plusMinutes(10).isAfter(LocalDateTime.now())) {
                verificationRepository.delete(entity);
                return;
            }
            verificationRepository.delete(entity);
            throw new UserBadRequestException("Verification Code has Expired!");
        }
        throw new UserBadRequestException("Wrong Verification Code!");
    }

    public StandardResponse<String> updatePassword(UpdatePasswordDto updatePasswordDto) {
        UserEntity user = userRepository.findByEmail(updatePasswordDto.getEmail())
                .orElseThrow(() -> new DataNotFoundException("User not found"));

        user.setPassword(passwordEncoder.encode(updatePasswordDto.getNewPassword()));
        user.setUpdatedDate(LocalDateTime.now());
        userRepository.save(user);
        return StandardResponse.<String>builder().status(Status.SUCCESS).message("Successfully updated").build();
    }

    public String generateVerificationCode() {
        Random random = new Random(System.currentTimeMillis());
        int code = random.nextInt(1000000);
        return String.format("%06d", code);
    }

    public void checkUserEmailAndPhoneNumber(String email, String phoneNumber) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new UserBadRequestException("email already exists");
        }
        if (userRepository.findUserEntityByPhoneNumber(phoneNumber).isPresent()) {
            throw new UserBadRequestException("phone number already exists");
        }
    }

    public StandardResponse<JwtResponse> getNewAccessToken(Principal principal) {
        UserEntity userEntity = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new DataNotFoundException("user not found"));
        String accessToken = jwtService.generateAccessToken(userEntity);
        JwtResponse jwtResponse = JwtResponse.builder().accessToken(accessToken).build();
        return StandardResponse.<JwtResponse>builder().status(Status.SUCCESS).message("Access token successfully sent").data(jwtResponse).build();
    }
    public UUID sendId(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new DataNotFoundException("User not found")).getId();
    }
    public StandardResponse<UserDetailsForFront> getMeByToken(String email) {
        UserEntity userEntity = userRepository.findByEmail(email).orElseThrow(() -> new DataNotFoundException("User not found"));
        return StandardResponse.<UserDetailsForFront>builder().status(Status.SUCCESS).message("User entity").data(mappingUser(userEntity)).build();
    }
    public String sendEmail(UUID userId){
        return userRepository.findById(userId).orElseThrow(() -> new DataNotFoundException("User not found")).getEmail();
    }
    public UUID sendHospitalId(String employeeEmail){
        UserEntity employee = userRepository.findByEmail(employeeEmail).orElseThrow(() -> new DataNotFoundException("User not found"));
        return employee.getEmployeeOfHospital();
    }
    public DoctorDetailsForBooking sendDoctor(UUID userId){
        UserEntity doctor = userRepository.getDoctorById(userId).orElseThrow(() -> new DataNotFoundException("Doctor not found"));
        return DoctorDetailsForBooking.builder()
                .roomNumber(doctor.getDoctorInfo().getRoomNumber())
                .fullName(doctor.getFullName())
                .specialty(doctor.getDoctorInfo().getDoctorSpecialty().getName())
                .gender(doctor.getGender().toString())
                .hospitalId(doctor.getEmployeeOfHospital())
                .id(doctor.getId())
                .build();
    }

    public UserResponseForFront sendUser(UUID userId) {
        UserEntity user = userRepository.getUserById(userId).orElseThrow(() -> new DataNotFoundException("User not found"));
        return UserResponseForFront.builder()
                .name(user.getFullName())
                .id(user.getId())
                .build();
    }

    public StandardResponse<Boolean> checkPassword(CheckPasswordDto checkPasswordDto, Principal principal) {
        UserEntity userEntity = userRepository.findByEmail(principal.getName()).orElseThrow(() -> new DataNotFoundException("User not found"));
        boolean matches = passwordEncoder.matches(checkPasswordDto.getPassword(), userEntity.getPassword());
        if(!matches) throw new UserBadRequestException("Password not matches");
        return StandardResponse.<Boolean>builder().status(Status.SUCCESS).message("Password matches").data(true).build();
    }


    public void setEmployment(SetEmployment dto) {
        UserEntity user = userRepository.findByEmail(dto.getEmail()).orElseThrow(() -> new DataNotFoundException("User not found"));
        user.setEmployeeOfHospital(dto.getHospitalId());
        userRepository.save(user);
    }
}
