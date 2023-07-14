package com.example.userservice.repository;

import com.example.userservice.entity.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity,UUID> {
    Optional<UserEntity> findUserEntityByEmail (String email);
}
