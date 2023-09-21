package com.example.userservice.controller;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

@WebMvcTest(RoleController.class)
public class RoleControllerTest {

//    @Autowired
//    private MockMvc mockMvc;
//
//    @Mock
//    private RoleService roleService;
//
//    @BeforeEach
//    public void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    @WithMockUser(roles = "SUPER_ADMIN")
//    public void testCreateRole() throws Exception {
//        RoleDto roleDto = new RoleDto();
//        StandardResponse<RoleEntity> mockResponse = StandardResponse.<RoleEntity>builder().build();
//
//        when(roleService.save(any(RoleDto.class))).thenReturn(mockResponse);
//
//        mockMvc.perform(post("/user/role/create")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(asJsonString(roleDto)))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
//    }
//
//    @Test
//    @WithMockUser(roles = "SUPER_ADMIN")
//    public void testGetRole() throws Exception {
//        String roleName = "ADMIN";
//        StandardResponse<RoleEntity> mockResponse = StandardResponse.<RoleEntity>builder().build();
//
//        when(roleService.getRole(anyString())).thenReturn(mockResponse);
//
//        mockMvc.perform(get("/user/role/get-role")
//                        .param("name", roleName))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
//    }
//    @Test
//    @WithMockUser(roles = "SUPER_ADMIN")
//    public void testUpdatePermissionsToRole() throws Exception {
//        RoleDto roleDto = new RoleDto();
//        StandardResponse<RoleEntity> mockResponse = StandardResponse.<RoleEntity>builder().build();
//
//        when(roleService.update(any(RoleDto.class))).thenReturn(mockResponse);
//
//        mockMvc.perform(put("/user/role/add-permissions-to-role")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(asJsonString(roleDto)))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
//    }
//
//    @Test
//    @WithMockUser(roles = "SUPER_ADMIN")
//    public void testAssignRoleToUser() throws Exception {
//        RoleAssignDto roleAssignDto = new RoleAssignDto();
//
//        String expectedResult = "Role successfully assigned to User";
//        StandardResponse<String> mockResponse = StandardResponse.<String>builder().build();
//
//        when(roleService.assignRoleToUser(any(RoleAssignDto.class), any(Principal.class)))
//                .thenReturn(mockResponse);
//        mockMvc.perform(post("/user/role/assign-role-to-user")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(asJsonString(roleAssignDto)))
//                .andExpect(status().isOk())
//                .andExpect(content().string(expectedResult));
//    }
//
//    @Test
//    @WithMockUser(roles = "OWNER")
//    public void testAssignHospital() throws Exception {
//        HospitalAssignDto hospitalAssignDto = new HospitalAssignDto();
//
//        String expectedResult = "Successfully assigned hospital";
//
//        mockMvc.perform(post("/user/role/assign-hospital")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(asJsonString(hospitalAssignDto)))
//                .andExpect(status().isOk())
//                .andExpect(content().string(expectedResult));
//
//        verify(roleService, times(1)).assignHospital(any(HospitalAssignDto.class));
//    }
//    private String asJsonString(Object obj) {
//        try {
//            ObjectMapper objectMapper = new ObjectMapper();
//            return objectMapper.writeValueAsString(obj);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
}
