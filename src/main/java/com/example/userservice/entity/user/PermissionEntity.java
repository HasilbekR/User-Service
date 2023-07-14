package com.example.userservice.entity.user;

import com.example.userservice.entity.BaseEntity;
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
