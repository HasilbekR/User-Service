package com.example.userservice.domain.entity.role;

import com.example.userservice.domain.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private List<PermissionEntity> permissions;
}
