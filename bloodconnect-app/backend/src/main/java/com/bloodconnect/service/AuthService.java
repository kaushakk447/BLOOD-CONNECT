package com.bloodconnect.service;

import com.bloodconnect.api.dto.*;
import com.bloodconnect.domain.entity.User;
import com.bloodconnect.exception.BadRequestException;
import com.bloodconnect.exception.ResourceNotFoundException;
import com.bloodconnect.repository.UserRepository;
import com.bloodconnect.security.JwtProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtProvider jwtProvider;
    
    @Autowired
    private AuditService auditService;
    
    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getEmail());
        
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            auditService.logAction("REGISTER", "User", null, null, request.toString(), "FAILURE", "Email already exists");
            throw new BadRequestException("Email already registered");
        }
        
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new BadRequestException("Phone already registered");
        }
        
        User user = User.builder()
            .email(request.getEmail())
            .phone(request.getPhone())
            .name(request.getName())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .role(User.UserRole.valueOf(request.getRole().toUpperCase()))
            .accountStatus(User.AccountStatus.ACTIVE)
            .verified(false)
            .emailVerified(false)
            .phoneVerified(false)
            .build();
        
        user = userRepository.save(user);
        auditService.logAction("REGISTER", "User", user.getId(), null, null, "SUCCESS");
        
        return mapUserToResponse(user);
    }
    
    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        log.info("User login attempt: {}", request.getEmail());
        
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            auditService.logAction("LOGIN", "User", user.getId(), null, null, "FAILURE", "Invalid password");
            throw new BadRequestException("Invalid email or password");
        }
        
        if (user.getAccountStatus() == User.AccountStatus.SUSPENDED) {
            throw new BadRequestException("Account is suspended");
        }
        
        String accessToken = jwtProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(user.getId(), user.getEmail());
        
        user.setLastLogin(java.time.LocalDateTime.now());
        userRepository.save(user);
        
        auditService.logAction("LOGIN", "User", user.getId(), null, null, "SUCCESS");
        
        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .user(mapUserToResponse(user))
            .build();
    }
    
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new BadRequestException("Invalid refresh token");
        }
        
        String email = jwtProvider.getUserEmailFromToken(refreshToken);
        Long userId = jwtProvider.getUserIdFromToken(refreshToken);
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        String newAccessToken = jwtProvider.generateToken(user.getId(), user.getEmail(), user.getRole().name());
        
        return AuthResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(refreshToken)
            .user(mapUserToResponse(user))
            .build();
    }
    
    private UserResponse mapUserToResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .phone(user.getPhone())
            .role(user.getRole().name())
            .build();
    }
}
