package com.example.userservice.repository;

import com.example.userservice.domain.entity.doctor.DoctorInfo;
import com.example.userservice.domain.entity.doctor.DoctorStatus;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface DoctorRepository extends JpaRepository<DoctorInfo, UUID> {
    @Modifying
    @Transactional
    @Query("update doctors_info d set d.status = :status where d.id in (select u.doctorInfo.id from users u where u.email = :email)")
    void update(@Param("status") DoctorStatus status, @Param("email") String email);

}
