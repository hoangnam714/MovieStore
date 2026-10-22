package com.btlon.movie.store.service;

import com.btlon.movie.store.dto.UserLoginRequest;
import com.btlon.movie.store.dto.UserRegisterRequest;
import com.btlon.movie.store.dto.UserResponse;
import com.btlon.movie.store.entity.User;
import com.btlon.movie.store.entity.UserRole;
import com.btlon.movie.store.repository.TicketRepository;
import com.btlon.movie.store.repository.UserRepository;
import com.btlon.movie.store.web.BadRequestException;
import com.btlon.movie.store.web.ConflictException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository, TicketRepository ticketRepository) {
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
    }

    public Optional<UserResponse> findById(Long id) {
        return userRepository.findById(id).map(UserResponse::from);
    }

    @Transactional
    public UserResponse register(UserRegisterRequest request) {
        validateRegister(request);
        if (userRepository.existsByEmail(request.getEmail().trim())) {
            throw new ConflictException("Email already registered");
        }
        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(request.getEmail().trim());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole() == null ? UserRole.CUSTOMER : request.getRole());
        return UserResponse.from(userRepository.save(user));
    }

    public Optional<UserResponse> login(UserLoginRequest request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            return Optional.empty();
        }
        return userRepository.findByEmail(request.getEmail().trim())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getPasswordHash()))
                .map(UserResponse::from);
    }

    @Transactional
    public boolean deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            return false;
        }
        if (ticketRepository.existsByUser_Id(userId)) {
            throw new ConflictException("Cannot delete user with tickets");
        }
        userRepository.deleteById(userId);
        return true;
    }

    private static void validateRegister(UserRegisterRequest request) {
        if (request.getFullName() == null || request.getFullName().isBlank()
                || request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("fullName, email and password are required");
        }
    }
}
