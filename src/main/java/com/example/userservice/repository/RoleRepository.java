package com.example.userservice.repository;

import com.example.userservice.domain.entity.role.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {
    List<RoleEntity> findRoleEntitiesByNameIn(List<String> roles);
    Optional<RoleEntity> findRoleEntityByName(String name);
    RoleEntity findRoleEntitiesByName(String name);

}
