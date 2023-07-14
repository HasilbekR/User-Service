package com.example.userservice.domain.entity.user;

import com.example.userservice.domain.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserEntity extends BaseEntity implements UserDetails {
    @Column(unique = true, nullable = false)
    private String email;
    private String name;
    @Column(nullable = false)
    private String password;
    @ManyToMany
    private List<RoleEntity> roles;
    @ManyToMany
    private List<PermissionEntity> permissions;
    @Enumerated(EnumType.STRING)
    private UserState state;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String ROLE="ROLE_";
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        for (RoleEntity role : roles) {
            authorities.add(new SimpleGrantedAuthority(ROLE + role));
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
