package com.btlon.movie.store.controller;

import com.btlon.movie.store.dto.ChangePasswordRequest;
import com.btlon.movie.store.dto.LoginResponse;
import com.btlon.movie.store.dto.ResetPasswordRequest;
import com.btlon.movie.store.dto.UserLoginRequest;
import com.btlon.movie.store.dto.UserRegisterRequest;
import com.btlon.movie.store.dto.UserResponse;
import com.btlon.movie.store.entity.User;
import com.btlon.movie.store.security.AdminOnly;
import com.btlon.movie.store.security.CurrentUser;
import com.btlon.movie.store.security.LoginRequired;
import com.btlon.movie.store.service.AuthService;
import com.btlon.movie.store.service.PasswordService;
import com.btlon.movie.store.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final AuthService authService;
    private final PasswordService passwordService;

    public UserController(UserService userService, AuthService authService, PasswordService passwordService) {
        this.userService = userService;
        this.authService = authService;
        this.passwordService = passwordService;
    }

    @GetMapping("/me")
    @LoginRequired
    public UserResponse me(@CurrentUser User user) {
        return UserResponse.from(user);
    }

    @GetMapping("/{id}")
    @AdminOnly
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return userService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody UserRegisterRequest request) {
        User user = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new LoginResponse(user, authService.issue(user)));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody UserLoginRequest request) {
        return userService.login(request)
                .map(user -> ResponseEntity.ok(new LoginResponse(user, authService.issue(user))))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) authService.revoke(authorization.substring(7).trim());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        passwordService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/password")
    @LoginRequired
    public ResponseEntity<Void> changePassword(@CurrentUser User user, @RequestBody ChangePasswordRequest request,
                                               @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization) {
        passwordService.changePassword(user, authorization.substring(7).trim(), request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @AdminOnly
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
