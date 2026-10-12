package com.example.foodordering.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecurityAndJwtTest {

    private JwtUtils jwtUtils;
    private final String secret = "CustomVerySecureSecretKeyForTestingPurpose1234567890!";
    private final long expirationMs = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils(secret, expirationMs);
    }

    @Test
    @DisplayName("Should generate valid JWT token and parse claims correctly")
    void testGenerateAndValidateToken() {
        String token = jwtUtils.generateToken("user@example.com", "user-123", "CUSTOMER", "Test User");

        assertNotNull(token);
        assertTrue(jwtUtils.validateToken(token));

        assertEquals("user@example.com", jwtUtils.getEmailFromToken(token));
        assertEquals("user-123", jwtUtils.getUserIdFromToken(token));
        assertEquals("CUSTOMER", jwtUtils.getRoleFromToken(token));
    }

    @Test
    @DisplayName("Should reject malformed or tampered JWT token")
    void testInvalidToken() {
        String tamperedToken = "eyJhbGciOiJIUzI1NiJ9.invalidpayload.invalidsignature";
        assertFalse(jwtUtils.validateToken(tamperedToken));
    }

    @Test
    @DisplayName("Should reject expired token")
    void testExpiredToken() {
        // Create JwtUtils with 1ms expiration
        JwtUtils shortLivedJwt = new JwtUtils(secret, -1000); // already expired
        String expiredToken = shortLivedJwt.generateToken("user@example.com", "user-123", "CUSTOMER", "Test User");

        assertFalse(shortLivedJwt.validateToken(expiredToken));
    }
}
