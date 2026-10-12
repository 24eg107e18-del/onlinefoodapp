package com.example.foodordering.service;

import com.example.foodordering.dto.AuthResponse;
import com.example.foodordering.dto.LoginRequest;
import com.example.foodordering.dto.RegisterRequest;
import com.example.foodordering.exception.DuplicateEmailException;
import com.example.foodordering.model.Role;
import com.example.foodordering.model.User;
import com.example.foodordering.repository.UserRepository;
import com.example.foodordering.security.JwtUtils;
import com.example.foodordering.security.UserDetailsImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException("An account with email " + normalizedEmail + " already exists");
        }

        // Public registration is ALWAYS forced to CUSTOMER role
        User user = new User(
                null,
                request.getName().trim(),
                normalizedEmail,
                passwordEncoder.encode(request.getPassword()),
                request.getPhone() != null ? request.getPhone().trim() : null,
                request.getAddress() != null ? request.getAddress().trim() : null,
                Role.CUSTOMER
        );

        User savedUser = userRepository.save(user);

        String jwt = jwtUtils.generateToken(
                savedUser.getEmail(),
                savedUser.getId(),
                savedUser.getRole().name(),
                savedUser.getName()
        );

        return new AuthResponse(
                jwt,
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtUtils.generateToken(
                userDetails.getEmail(),
                userDetails.getId(),
                userDetails.getRole().name(),
                userDetails.getName()
        );

        return new AuthResponse(
                jwt,
                userDetails.getId(),
                userDetails.getName(),
                userDetails.getEmail(),
                userDetails.getRole()
        );
    }
}
