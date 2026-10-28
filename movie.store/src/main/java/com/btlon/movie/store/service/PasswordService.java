package com.btlon.movie.store.service;

import com.btlon.movie.store.dto.ChangePasswordRequest;
import com.btlon.movie.store.dto.ResetPasswordRequest;
import com.btlon.movie.store.entity.User;
import com.btlon.movie.store.repository.UserRepository;
import com.btlon.movie.store.web.BadRequestException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PasswordService {
    private static final int MIN_LENGTH = 6;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    public PasswordService(UserRepository users, PasswordEncoder passwordEncoder, AuthService authService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }

    public static void validateNewPassword(String password, String confirmPassword) {
        if (password == null || password.length() < MIN_LENGTH)
            throw new BadRequestException("Password must be at least 6 characters");
        if (!password.equals(confirmPassword)) throw new BadRequestException("Password confirmation does not match");
    }

    /**
     * Resets the password when email and registered phone match, then logs the
     * account out everywhere.
     * One message for every mismatch so the form cannot reveal which emails exist.
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().isBlank() || request.getPhone() == null
                || request.getPhone().isBlank())
            throw new BadRequestException("email and phone are required");
        String phone = PhoneNumbers.normalize(request.getPhone());
        validateNewPassword(request.getNewPassword(), request.getConfirmPassword());

        User user = users.findByEmail(request.getEmail().trim()).filter(found -> phone.equals(found.getPhone()))
                .orElseThrow(() -> new BadRequestException("Email or phone number is incorrect"));
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        users.save(user);
        authService.revokeOtherSessions(user.getId(), null);
    }

    @Transactional
    public void changePassword(User user, String currentToken, ChangePasswordRequest request) {
        if (request == null || request.getCurrentPassword() == null
                || !passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash()))
            throw new BadRequestException("Current password is incorrect");
        validateNewPassword(request.getNewPassword(), request.getConfirmPassword());

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash()))
            throw new BadRequestException("New password must be different from the current password");
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        users.save(user);
        authService.revokeOtherSessions(user.getId(), currentToken);
    }
}
