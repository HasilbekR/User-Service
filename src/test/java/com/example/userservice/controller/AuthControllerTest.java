package com.example.userservice.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

import com.example.userservice.domain.dto.request.user.*;
import com.example.userservice.domain.dto.response.JwtResponse;
import com.example.userservice.domain.dto.response.StandardResponse;
import com.example.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSignUp() throws Exception {
        UserRequestDto userDto = new UserRequestDto();

        StandardResponse<JwtResponse> mockResponse = StandardResponse.<JwtResponse>builder().build();

        when(userService.save(any(UserRequestDto.class))).thenReturn(mockResponse);

        mockMvc.perform(post("/user/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(userDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testSignIn() throws Exception {
        LoginRequestDto loginDto = new LoginRequestDto();

        when(userService.signIn(any(LoginRequestDto.class))).thenReturn(new JwtResponse());

        mockMvc.perform(post("/user/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(loginDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    @Test
    public void testVerify() throws Exception {
        String code = "12345";
        String principalName = "example@example.com";

        when(userService.verify(any(Principal.class), anyString())).thenReturn("Verification successful");

        mockMvc.perform(get("/user/auth/verify")
                        .param("code", code)
                        .principal(new TestPrincipal(principalName)))
                .andExpect(status().isOk())
                .andExpect(content().string("Verification successful"));
    }

    @Test
    public void testSendVerificationCode() throws Exception {
        String principalName = "example@example.com";

        mockMvc.perform(get("/user/auth/send-verification-code")
                        .principal(new TestPrincipal(principalName)))
                .andExpect(status().isOk());

        verify(userService, times(1)).sendVerificationCode(principalName);
    }

    @Test
    public void testGetAccessToken() throws Exception {
        String principalName = "example@example.com";

        JwtResponse jwtResponse = new JwtResponse();

        when(userService.getNewAccessToken(any(Principal.class))).thenReturn(jwtResponse);

        mockMvc.perform(get("/user/auth/access-token")
                        .principal(new TestPrincipal(principalName)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testRefreshAccessToken() throws Exception {
        String principalName = "example@example.com";

        JwtResponse jwtResponse = new JwtResponse();



        when(userService.getNewAccessToken(any(Principal.class))).thenReturn(jwtResponse);

        mockMvc.perform(get("/user/auth/refresh-token")
                        .principal(new TestPrincipal(principalName)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testForgottenPassword() throws Exception {
        String email = "example@example.com";

        mockMvc.perform(get("/user/auth/forgot-password")
                        .param("email", email))
                .andExpect(status().isOk());
        verify(userService, times(1)).forgottenPassword(email);
    }

    @Test
    public void testVerifyCodeForUpdatePassword() throws Exception {
        VerifyCodeDto verifyCodeDto = new VerifyCodeDto();

        when(userService.verifyPasswordForUpdatePassword(any(VerifyCodeDto.class)))
                .thenReturn("Verification successful");

        mockMvc.perform(post("/user/auth/verify-code-for-update-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(verifyCodeDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Verification successful"));
    }

    @Test
    public void testUpdatePassword() throws Exception {
        UpdatePasswordDto updatePasswordDto = new UpdatePasswordDto();

        when(userService.updatePassword(any(UpdatePasswordDto.class))).thenReturn("Password updated successfully");

        mockMvc.perform(put("/user/auth/update-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(updatePasswordDto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Password updated successfully"));
    }

    private String asJsonString(Object obj) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private class TestPrincipal implements Principal {
        private final String name;

        public TestPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
