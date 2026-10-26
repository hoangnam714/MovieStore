package com.btlon.movie.store.dto;

import com.btlon.movie.store.entity.User;
import com.btlon.movie.store.entity.UserRole;

public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private UserRole role;

    public static UserResponse from(User user) {
        UserResponse response = new UserResponse();
        response.id = user.getId();
        response.fullName = user.getFullName();
        response.email = user.getEmail();
        response.phone = user.getPhone();
        response.role = user.getRole();
        return response;
    }

    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public UserRole getRole() { return role; }
}
