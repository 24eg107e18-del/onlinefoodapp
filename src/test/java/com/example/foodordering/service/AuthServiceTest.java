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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest(
                "Jane Doe",
                "jane@example.com",
                "secret123",
                "9876543210",
                "123 Maple Street"
        );

        loginRequest = new LoginRequest(
                "jane@example.com",
                "secret123"
        );
    }

    @Test
    @DisplayName("Should successfully register a new customer with hashed password")
    void testRegisterSuccess() {
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("hashedPassword123");

        User savedUser = new User(
                "user-1",
                "Jane Doe",
                "jane@example.com",
                "hashedPassword123",
                "9876543210",
                "123 Maple Street",
                Role.CUSTOMER
        );
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtUtils.generateToken("jane@example.com", "user-1", "CUSTOMER", "Jane Doe"))
                .thenReturn("mock-jwt-token");

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("user-1", response.getId());
        assertEquals("Jane Doe", response.getName());
        assertEquals("jane@example.com", response.getEmail());
        assertEquals(Role.CUSTOMER, response.getRole());

        verify(passwordEncoder, times(1)).encode("secret123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should reject registration when email is already registered")
    void testRegisterDuplicateEmail() {
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should successfully login with valid credentials")
    void testLoginSuccess() {
        UserDetailsImpl userDetails = new UserDetailsImpl(
                "user-1",
                "Jane Doe",
                "jane@example.com",
                "hashedPassword",
                Role.CUSTOMER,
                Collections.emptyList()
        );
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateToken("jane@example.com", "user-1", "CUSTOMER", "Jane Doe"))
                .thenReturn("mock-jwt-token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("user-1", response.getId());
        assertEquals(Role.CUSTOMER, response.getRole());
    }

    @Test
    @DisplayName("Should throw BadCredentialsException when password is wrong")
    void testLoginFailure() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }
}
