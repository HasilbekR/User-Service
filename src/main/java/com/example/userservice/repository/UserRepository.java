package com.example.userservice.repository;

import com.example.userservice.domain.entity.user.UserEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findUserEntityByPhoneNumber(String phoneNumber);
    @Query(value = "select u from users u join u.roles r where r.name = 'DOCTOR'")
    Page<UserEntity> getAllDoctorsFromHospital(UUID hospitalId, Pageable pageable);
    @Query(value = "select u from users u join u.roles r where r.name = 'DOCTOR' and u.email = ?1")
    Optional<UserEntity> getDoctorByEmail(String email);
    @Query(value = "select u from users u join u.roles r where r.name = 'DOCTOR' and u.id = ?1")
    Optional<UserEntity> getDoctorById(UUID id);


}

