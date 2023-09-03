package com.example.userservice.repository;

import com.example.userservice.domain.entity.doctor.DoctorSpecialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorSpecialtyRepository extends JpaRepository<DoctorSpecialty, UUID> {
    Optional<DoctorSpecialty> findDoctorSpecialtyByName(String name);
    List<DoctorSpecialty> findAll();
}
