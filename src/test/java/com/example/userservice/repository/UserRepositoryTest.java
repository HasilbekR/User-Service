package com.example.userservice.repository;

import com.example.userservice.domain.entity.user.UserEntity;
import com.example.userservice.domain.entity.user.UserState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
class UserRepositoryTest {
    private UserEntity userEntity;
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp(){
        userEntity = UserEntity.builder()
                .email("test")
                .fullName("test")
                .password("test")
                .state(UserState.UNVERIFIED)
                .build();
    }

    @Test
    void saveUserSuccess(){
        UserEntity user = userRepository.save(userEntity);
        assertNotNull(user.getId());
    }
    @Test
    void userExistsEmailTest(){
        userRepository.save(userEntity);
        Optional<UserEntity> byEmail = userRepository.findByEmail(userEntity.getEmail());
        assertTrue(byEmail.isPresent());
    }
    @Test
    void saveUserFailOnEmailTest(){
        userRepository.save(userEntity);
        userEntity.setId(null);
        UserEntity save = userRepository.save(userEntity);
        assertNull(save.getId());
    }



}