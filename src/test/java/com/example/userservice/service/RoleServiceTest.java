package com.example.userservice.service;

import com.example.userservice.domain.dto.request.RoleDto;
import com.example.userservice.domain.entity.user.PermissionEntity;
import com.example.userservice.domain.entity.user.RoleEntity;
import com.example.userservice.repository.PermissionRepository;
import com.example.userservice.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
class RoleServiceTest {
    private RoleEntity roleEntity;
    private PermissionEntity permissionEntity;
    private RoleDto roleDto;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    @BeforeEach
    void setUp(){
        roleDto = RoleDto.builder()
                .name("TEST")
                .permissions(List.of("TEST"))
                .build();
    }
    @Test
    void saveNewRoleNewPermissionsTest(){
        List<String> permissions = roleDto.getPermissions();
        List<PermissionEntity> rolePermissions = new ArrayList<>();
        for (String permission : permissions) {
            permissionEntity = PermissionEntity.builder().permission(permission).build();
            PermissionEntity save = permissionRepository.save(permissionEntity);
            rolePermissions.add(save);
            assertNotNull(save.getId());
            System.out.println(save);
        }
        roleEntity = RoleEntity.builder()
                .name(roleDto.getName())
                .permissions(rolePermissions)
                .build();
        RoleEntity role = roleRepository.save(roleEntity);
        assertNotNull(role.getId());
        System.out.println(role);
    }

    @Test
    void saveNewRoleExistingPermissionsTest(){
        List<String> permissions = roleDto.getPermissions();
        for (String permission : permissions) {
            permissionRepository.save(PermissionEntity.builder().permission(permission).build());
        }
        List<PermissionEntity> rolePermissions = permissionRepository.findPermissionEntitiesByPermissionIn(permissions);
        roleEntity = RoleEntity.builder()
                .name(roleDto.getName())
                .permissions(rolePermissions)
                .build();
        RoleEntity role = roleRepository.save(roleEntity);
        assertNotNull(role.getId());
        System.out.println(role);
    }
    //Adding new permissions in database
    @Test
    void saveExistingRoleAddNewPermissionsTest(){
        saveNewRoleNewPermissionsTest();
        roleDto.setPermissions(List.of("TEST2"));
        RoleEntity role = roleRepository.findRoleEntitiesByName(roleDto.getName());
        List<String> dtoPermissions = roleDto.getPermissions();
        List<PermissionEntity> rolePermissions = role.getPermissions();

        for (String dtoPermission : dtoPermissions) {
            PermissionEntity save = permissionRepository.save(PermissionEntity.builder().permission(dtoPermission).build());
            rolePermissions.add(save);
        }
        role.setPermissions(rolePermissions);
        RoleEntity roleEntity1 = roleRepository.save(role);
        assertEquals(2, roleEntity1.getPermissions().size());
        System.out.println(roleEntity1);
    }
    @Test
    void saveExistingRoleExistingPermissionsTest(){
        saveNewRoleNewPermissionsTest();

        permissionEntity.setPermission("TEST2");
        permissionEntity = permissionRepository.save(permissionEntity);

        RoleEntity role = roleRepository.findRoleEntitiesByName(roleDto.getName());
        List<PermissionEntity> permissions = role.getPermissions();
        permissions.add(permissionEntity);
        role.setPermissions(permissions);
        RoleEntity save = roleRepository.save(role);
        assertEquals(2,save.getPermissions().size());
    }
    @Test
    void deleteRoleSuccessTest(){
        saveNewRoleNewPermissionsTest();
        roleRepository.delete(roleRepository.findRoleEntityByName(roleDto.getName()).orElseThrow());
        RoleEntity roleEntitiesByName = roleRepository.findRoleEntitiesByName(roleDto.getName());
        assertNull(roleEntitiesByName);
    }
    @Test
    void deleteRoleFailTest(){
        Optional<RoleEntity> roleEntityByName = roleRepository.findRoleEntityByName(roleDto.getName());
        assertThrows(NoSuchElementException.class, ()->roleRepository.delete(roleEntityByName.orElseThrow()));
    }
    @Test
    void getRoleByNameSuccessTest(){
        saveNewRoleNewPermissionsTest();
        Optional<RoleEntity> roleEntityByName = roleRepository.findRoleEntityByName(roleDto.getName());
        assertTrue(roleEntityByName.isPresent());
    }
    @Test
    void getRoleByNameFailTest(){
        Optional<RoleEntity> roleEntityByName = roleRepository.findRoleEntityByName(roleDto.getName());
        assertTrue(roleEntityByName.isEmpty());
    }
    @Test
    void getRolesByNameSuccessTest(){
        List<PermissionEntity> rolePermissions = new ArrayList<>();
        for (String dtoPermission : roleDto.getPermissions()) {
            permissionEntity = PermissionEntity.builder()
                    .permission(dtoPermission)
                    .build();
            rolePermissions.add(permissionRepository.save(permissionEntity));
        }
        roleEntity = RoleEntity.builder()
                .name(roleDto.getName())
                .permissions(rolePermissions)
                .build();
        RoleEntity save = roleRepository.save(roleEntity);

        roleEntity = RoleEntity.builder()
                .name("TEST2")
                .permissions(rolePermissions)
                .build();
        RoleEntity save1 = roleRepository.save(roleEntity);
        List<String> roles = new ArrayList<>(List.of(save.getName(),save1.getName()));
        List<RoleEntity> roleEntitiesByNameIn = roleRepository.findRoleEntitiesByNameIn(roles);
        System.out.println(roleEntitiesByNameIn);
        assertEquals(2,roleEntitiesByNameIn.size());
    }

    @Test
    void updateRoleNameSuccessTest(){
        saveNewRoleNewPermissionsTest();
        Optional<RoleEntity> roleEntityOptional = roleRepository.findRoleEntityByName(roleDto.getName());
        assertTrue(roleEntityOptional.isPresent());

        roleDto.setName("UPDATED_TEST");
        Optional<RoleEntity> roleEntityByName = roleRepository.findRoleEntityByName(roleDto.getName());
        assertTrue(roleEntityByName.isEmpty());

        roleEntityOptional.orElseThrow().setName(roleDto.getName());
        RoleEntity role = roleRepository.save(roleEntityOptional.orElseThrow());
        assertEquals("UPDATED_TEST",role.getName());
    }

    @Test
    void updateRoleNameFailTest(){
        saveNewRoleNewPermissionsTest();
        Optional<RoleEntity> roleEntityOptional = roleRepository.findRoleEntityByName(roleDto.getName());
        assertTrue(roleEntityOptional.isPresent());

        roleDto.setName("UPDATED_TEST");
        saveNewRoleExistingPermissionsTest();
        Optional<RoleEntity> roleEntityByName = roleRepository.findRoleEntityByName(roleDto.getName());
        assertTrue(roleEntityByName.isPresent());
    }

    @Test
    void updateRolePermissionsSuccessTest(){
        saveNewRoleNewPermissionsTest();
        List<PermissionEntity> updatedPermissions = new ArrayList<>();
        Optional<RoleEntity> roleEntityByName = roleRepository.findRoleEntityByName(roleDto.getName());
        roleDto.setPermissions(List.of("UPDATED_TEST"));

        for (String permission : roleDto.getPermissions()) {
            permissionEntity = permissionRepository.findPermissionEntitiesByPermission(permission);
            if(permissionEntity !=null){
                updatedPermissions.add(permissionEntity);
            }else {
                permissionEntity = PermissionEntity.builder().permission(permission).build();
                PermissionEntity save = permissionRepository.save(permissionEntity);
                updatedPermissions.add(save);
            }
        }
        roleEntityByName.orElseThrow().setPermissions(updatedPermissions);
        assertEquals(1, roleEntityByName.orElseThrow().getPermissions().size());
        System.out.println(roleEntityByName);
    }


}