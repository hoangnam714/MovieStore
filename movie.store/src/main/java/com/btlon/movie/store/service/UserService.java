package com.btlon.movie.store.service;

import com.btlon.movie.store.dto.UserLoginRequest;
import com.btlon.movie.store.dto.UserRegisterRequest;
import com.btlon.movie.store.dto.UserResponse;
import com.btlon.movie.store.entity.User;
import com.btlon.movie.store.entity.UserRole;
import com.btlon.movie.store.repository.AuthTokenRepository;
import com.btlon.movie.store.repository.TicketRepository;
import com.btlon.movie.store.repository.UserRepository;
import com.btlon.movie.store.web.BadRequestException;
import com.btlon.movie.store.web.ConflictException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final AuthTokenRepository authTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, TicketRepository ticketRepository,
                       AuthTokenRepository authTokenRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.authTokenRepository = authTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<UserResponse> findById(Long id) {
        return userRepository.findById(id).map(UserResponse::from);
    }

    @Transactional
    public User register(UserRegisterRequest request) {
        validateRegister(request);
        String phone = PhoneNumbers.normalize(request.getPhone());
        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new ConflictException("Email already registered");
        }
        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(request.getEmail().trim());
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.CUSTOMER);
        return userRepository.save(user);
    }

    public Optional<User> login(UserLoginRequest request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            return Optional.empty();
        }
        return userRepository.findByEmail(request.getEmail().trim())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPasswordHash()));
    }

    @Transactional
    public boolean deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            return false;
        }
        if (ticketRepository.existsByUser_Id(userId)) {
            throw new ConflictException("Cannot delete user with tickets");
        }
        authTokenRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
        return true;
    }

    private static void validateRegister(UserRegisterRequest request) {
        if (request.getFullName() == null || request.getFullName().isBlank()
                || request.getEmail() == null || request.getEmail().isBlank()
                || request.getPhone() == null || request.getPhone().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("fullName, email, phone and password are required");
        }
        PasswordService.validateNewPassword(request.getPassword(), request.getConfirmPassword());
    }
}
