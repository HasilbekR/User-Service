package com.example.userservice.domain.entity.role;

import com.example.userservice.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity(name = "role")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleEntity extends BaseEntity {
    private String name;
    @ManyToMany
    private List<PermissionEntity> permissions;
}
