package com.keke125.pixel.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.keke125.pixel.data.Role;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


import java.util.Collection;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "application_user")
public class User extends AbstractEntity implements UserDetails {
    @NotNull
    @Length(min = 1, max = 32)
    @Column(unique = true)
    private String username;
    @NotNull
    @Length(min = 1, max = 32)
    private String name;
    @NotNull
    @JsonIgnore
    private String hashedPassword;
    @NotNull
    @Enumerated(EnumType.STRING)
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Role> roles;
    // if avatar is more than 3MB, need to modify column length
    @Lob
    @Column(length = 3200000)
    private byte[] avatarImage;
    private String avatarImageName;
    @NotNull
    @Email
    @Column(unique = true)
    private String email;
    private boolean enabled;
    private boolean isAccountNonExpired;
    private boolean isAccountNonLocked;
    private boolean isCredentialsNonExpired;
    // this value can't be changed by any user
    private Double imageSize;
    // this value can be changed by user management page
    private Double imageSizeLimit;


    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return this.hashedPassword;
    }

    public boolean isAdmin() {
        return roles.contains(Role.ADMIN);
    }

    public void setAdmin(boolean bool) {
        if (!bool && isAdmin()) {
            roles.remove(Role.ADMIN);
        } else if (bool && !isAdmin()) {
            roles.add(Role.ADMIN);
        }
    }

    public @NotNull String getUsername() {
        return username;
    }

    public void setUsernane(String username) {
        this.username = username;
    }
}
