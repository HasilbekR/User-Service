package com.example.userservice.domain.entity.user;

import com.example.userservice.domain.entity.BaseEntity;
import com.example.userservice.domain.entity.doctor.DoctorInfo;
import com.example.userservice.domain.entity.role.PermissionEntity;
import com.example.userservice.domain.entity.role.RoleEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class UserEntity extends BaseEntity implements UserDetails {
    @Column(unique = true, nullable = false)
    private String email;
    private String fullName;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private LocalDate dateOfBirth;
    @Column(nullable = false, unique = true)
    private String phoneNumber;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @ManyToMany(cascade = CascadeType.ALL)
    private List<RoleEntity> roles;
    @ManyToMany(cascade = CascadeType.ALL)
    private List<PermissionEntity> permissions;
    @Enumerated(EnumType.STRING)
    private UserState state;
    @Enumerated(EnumType.STRING)
    private UserReservationData userReservationData;
    @OneToOne
    private DoctorInfo doctorInfo;
    private UUID employeeOfHospital;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String ROLE="ROLE_";
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (RoleEntity role : roles) {
            authorities.add(new SimpleGrantedAuthority(ROLE + role.getName()));
        }
        for (PermissionEntity permission : permissions) {
            authorities.add(new SimpleGrantedAuthority(permission.getPermission()));
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
