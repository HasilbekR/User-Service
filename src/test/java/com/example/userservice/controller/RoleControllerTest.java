package com.example.userservice.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;

import com.example.userservice.domain.dto.request.role.HospitalAssignDto;
import com.example.userservice.domain.dto.request.role.RoleAssignDto;
import com.example.userservice.domain.dto.request.role.RoleDto;
import com.example.userservice.domain.entity.role.RoleEntity;
import com.example.userservice.service.RoleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.security.Principal;

@WebMvcTest(RoleController.class)
public class RoleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private RoleService roleService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    public void testCreateRole() throws Exception {
        RoleDto roleDto = new RoleDto();

        when(roleService.save(any(RoleDto.class))).thenReturn(new RoleEntity());

        mockMvc.perform(post("/user/role/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(roleDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    public void testGetRole() throws Exception {
        String roleName = "ADMIN";

        when(roleService.getRole(anyString())).thenReturn(new RoleEntity());

        mockMvc.perform(get("/user/role/get-role")
                        .param("name", roleName))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    public void testUpdatePermissionsToRole() throws Exception {
        RoleDto roleDto = new RoleDto();

        when(roleService.update(any(RoleDto.class))).thenReturn(new RoleEntity());

        mockMvc.perform(put("/user/role/add-permissions-to-role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(roleDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    public void testAssignRoleToUser() throws Exception {
        RoleAssignDto roleAssignDto = new RoleAssignDto();

        String expectedResult = "Role successfully assigned to User";

        when(roleService.assignRoleToUser(any(RoleAssignDto.class), any(Principal.class)))
                .thenReturn("User");

        mockMvc.perform(post("/user/role/assign-role-to-user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(roleAssignDto)))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResult));
    }

    @Test
    @WithMockUser(roles = "OWNER")
    public void testAssignHospital() throws Exception {
        HospitalAssignDto hospitalAssignDto = new HospitalAssignDto();

        String expectedResult = "Successfully assigned hospital";

        mockMvc.perform(post("/user/role/assign-hospital")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(hospitalAssignDto)))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedResult));

        verify(roleService, times(1)).assignHospital(any(HospitalAssignDto.class));
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
