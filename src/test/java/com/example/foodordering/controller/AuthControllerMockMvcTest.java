package com.example.foodordering.controller;

import com.example.foodordering.dto.AuthResponse;
import com.example.foodordering.dto.LoginRequest;
import com.example.foodordering.dto.RegisterRequest;
import com.example.foodordering.exception.DuplicateEmailException;
import com.example.foodordering.exception.GlobalExceptionHandler;
import com.example.foodordering.model.Role;
import com.example.foodordering.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerMockMvcTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("POST /api/auth/register should return 201 Created on valid input")
    void testRegisterSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Jane Doe",
                "jane@example.com",
                "secret123",
                "1234567890",
                "123 Food Street"
        );

        AuthResponse authResponse = new AuthResponse(
                "mock-token-xyz",
                "user-1",
                "Jane Doe",
                "jane@example.com",
                Role.CUSTOMER
        );

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("mock-token-xyz"))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    @DisplayName("POST /api/auth/register should return 400 Bad Request on invalid email")
    void testRegisterInvalidEmail() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Jane",
                "not-an-email",
                "secret123",
                "1234567890",
                "123 Food Street"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    @DisplayName("POST /api/auth/register should return 409 Conflict on duplicate email")
    void testRegisterDuplicateEmail() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Jane Doe",
                "jane@example.com",
                "secret123",
                "1234567890",
                "123 Food Street"
        );

        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new DuplicateEmailException("An account with email jane@example.com already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @DisplayName("POST /api/auth/login should return 200 OK on valid credentials")
    void testLoginSuccess() throws Exception {
        LoginRequest request = new LoginRequest("jane@example.com", "secret123");
        AuthResponse authResponse = new AuthResponse(
                "jwt-login-token",
                "user-1",
                "Jane Doe",
                "jane@example.com",
                Role.CUSTOMER
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-login-token"))
                .andExpect(jsonPath("$.email").value("jane@example.com"));
    }
}
