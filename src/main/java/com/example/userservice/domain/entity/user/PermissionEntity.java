package com.example.userservice.domain.entity.user;

import com.example.userservice.domain.entity.BaseEntity;
import jakarta.persistence.Entity;
import lombok.*;

@Entity(name = "permission")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PermissionEntity extends BaseEntity {
    private String permission;
}
