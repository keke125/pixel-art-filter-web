package com.keke125.pixel.data.entity;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.Email;

@Entity
public class UserDetail extends AbstractEntity {

    private String name;
    @Email
    private String email;
    private boolean enabled;
    private boolean admin;
    private Integer imageSize;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public boolean isAdmin() {
        return admin;
    }
    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
    public Integer getImageSize() {
        return imageSize;
    }
    public void setImageSize(Integer imageSize) {
        this.imageSize = imageSize;
    }

}
