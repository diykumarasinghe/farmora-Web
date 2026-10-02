package com.farmora.service;

import com.farmora.dto.auth.AuthResponse;
import com.farmora.dto.auth.ChangePasswordRequest;
import com.farmora.dto.auth.LoginRequest;
import com.farmora.dto.auth.RegisterRequest;
import com.farmora.dto.auth.UpdateProfileRequest;
import com.farmora.dto.auth.UserResponse;
import com.farmora.exception.BusinessException;
import com.farmora.exception.ResourceNotFoundException;
import com.farmora.model.Role;
import com.farmora.model.User;
import com.farmora.repository.UserRepository;
import com.farmora.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.getRole() == Role.ADMIN) {
            throw new BusinessException("Public registration as ADMIN is not allowed.");
        }

        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BusinessException("An account with this email already exists.");
        }

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(request.getEmail().trim().toLowerCase())
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .address(request.getAddress() != null ? request.getAddress().trim() : null)
                .buyerType(request.getBuyerType())
                .businessName(request.getBusinessName() != null ? request.getBusinessName().trim() : null)
                .businessRegNumber(request.getBusinessRegNumber() != null ? request.getBusinessRegNumber().trim() : null)
                .active(true)
                .build();

        User savedUser = userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getEmail());
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", savedUser.getRole().name());
        extraClaims.put("userId", savedUser.getId());
        String token = jwtService.generateToken(userDetails, extraClaims);

        return AuthResponse.builder()
                .token(token)
                .user(mapToUserResponse(savedUser))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (!user.getActive()) {
            throw new BusinessException("Your account has been deactivated. Please contact support.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("userId", user.getId());
        String token = jwtService.generateToken(userDetails, extraClaims);

        return AuthResponse.builder()
                .token(token)
                .user(mapToUserResponse(user))
                .build();
    }

    public UserResponse getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        user.setFullName(request.getFullName().trim());
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress().trim());
        }
        if (request.getProfileImageUrl() != null) {
            user.setProfileImageUrl(request.getProfileImageUrl().trim());
        }
        if (request.getBuyerType() != null) {
            user.setBuyerType(request.getBuyerType());
        }
        if (request.getBusinessName() != null) {
            user.setBusinessName(request.getBusinessName().trim());
        }
        if (request.getBusinessRegNumber() != null) {
            user.setBusinessRegNumber(request.getBusinessRegNumber().trim());
        }

        User updated = userRepository.save(user);
        return mapToUserResponse(updated);
    }

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("Current password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole())
                .address(user.getAddress())
                .profileImageUrl(user.getProfileImageUrl())
                .buyerType(user.getBuyerType())
                .businessName(user.getBusinessName())
                .businessRegNumber(user.getBusinessRegNumber())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
