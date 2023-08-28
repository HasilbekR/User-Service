package com.example.userservice.controller;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

import com.example.userservice.domain.dto.request.DoctorCreateDto;
import com.example.userservice.domain.dto.request.ExchangeDataDto;
import com.example.userservice.domain.dto.request.user.UserRequestDto;
import com.example.userservice.domain.entity.doctor.DoctorAvailability;
import com.example.userservice.domain.entity.doctor.DoctorStatus;
import com.example.userservice.domain.entity.user.UserEntity;
import com.example.userservice.service.DoctorService;
import com.example.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;

import java.security.Principal;
import java.util.ArrayList;
import java.util.UUID;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private DoctorService doctorService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testAddDoctor() throws Exception {
        DoctorCreateDto drCreateDto = new DoctorCreateDto();

        when(doctorService.saveDoctor(any(DoctorCreateDto.class), any(BindingResult.class), any(Principal.class)))
                .thenReturn(new UserEntity());

        mockMvc.perform(post("/user/add-doctor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(drCreateDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testUpdateUserProfile() throws Exception {
        UUID userId = UUID.randomUUID();
        UserRequestDto update = new UserRequestDto();

        when(userService.updateProfile(any(UUID.class), any(UserRequestDto.class))).thenReturn(new UserEntity());

        mockMvc.perform(put("/user/{userId}/update-user", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(update)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    @Test
    public void testExchangeId() throws Exception {
        ExchangeDataDto exchangeDataDto = new ExchangeDataDto();
        exchangeDataDto.setSource("example@example.com");

        UserEntity userEntity = new UserEntity();
        userEntity.setId(UUID.randomUUID());

        when(userService.findByEmail(anyString())).thenReturn(userEntity);

        mockMvc.perform(post("/user/send-id")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(exchangeDataDto)))
                .andExpect(status().isOk())
                .andExpect(content().string(userEntity.getId().toString()));
    }

    @Test
    public void testExchangeEmail() throws Exception {
        ExchangeDataDto exchangeDataDto = new ExchangeDataDto();
        exchangeDataDto.setSource(UUID.randomUUID().toString());

        UserEntity userEntity = new UserEntity();
        userEntity.setEmail("example@example.com");

        when(userService.findById(any(UUID.class))).thenReturn(userEntity);

        mockMvc.perform(post("/user/send-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(exchangeDataDto)))
                .andExpect(status().isOk())
                .andExpect(content().string(userEntity.getEmail()));
    }

    @Test
    public void testGetAllDoctorsFromHospital() throws Exception {
        UUID hospitalId = UUID.randomUUID();

        when(doctorService.getAllDoctor(anyInt(), anyInt(), any(UUID.class)))
                .thenReturn(new ArrayList<>());

        mockMvc.perform(get("/user/get-all-doctors-from-hospital")
                        .param("hospitalId", hospitalId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = {"DOCTOR", "ADMIN"})
    public void testChangeDoctorStatus() throws Exception {
        String email = "doctor@example.com";
        String status = "ACTIVE";

        when(doctorService.updateDoctorStatus(anyString(), any(DoctorStatus.class)))
                .thenReturn(HttpStatus.OK);

        mockMvc.perform(put("/user/change-doctor-status")
                        .param("email", email)
                        .param("status", status))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    public void testDeleteDoctorFromHospital() throws Exception {
        String email = "doctor@example.com";

        when(doctorService.deleteDoctorFromHospital(anyString()))
                .thenReturn(HttpStatus.OK);

        mockMvc.perform(delete("/user/delete-doctor-from-hospital")
                        .param("email", email))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "DOCTOR")
    public void testSetDoctorAvailability() throws Exception {
        DoctorAvailability doctorAvailability = new DoctorAvailability();


        when(doctorService.setAvailability(any(DoctorAvailability.class), any(Principal.class), any(BindingResult.class)))
                .thenReturn(ResponseEntity.ok("Availability set successfully"));

        mockMvc.perform(post("/user/set-doctor-availability")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(doctorAvailability)))
                .andExpect(status().isOk())
                .andExpect(content().string("Availability set successfully"));
    }

    private String asJsonString(Object obj) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
