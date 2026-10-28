package com.btlon.movie.store.dto;

import com.btlon.movie.store.entity.User;
import com.btlon.movie.store.entity.UserRole;

/**
 * User profile plus the bearer token to send as "Authorization: Bearer
 * <token>".
 */
public class LoginResponse {
    private final Long id;
    private final String fullName;
    private final String email;
    private final String phone;
    private final UserRole role;
    private final String token;

    public LoginResponse(User user, String token) {
        this.id = user.getId();
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.role = user.getRole();
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public UserRole getRole() {
        return role;
    }

    public String getToken() {
        return token;
    }
}
