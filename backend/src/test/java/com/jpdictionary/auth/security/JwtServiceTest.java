package com.jpdictionary.auth.security;

import com.jpdictionary.auth.enums.Role;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    private JwtService jwtService;
    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(SECRET);
        properties.setExpirationMs(3600000L);
        jwtService = new JwtService(properties);
        principal = new UserPrincipal(
                1L,
                "komorebi_user",
                "user@komorebi.jp",
                "encoded",
                Role.USER,
                true
        );
    }

    @Test
    @DisplayName("Should generate and validate JWT token for matching user")
    void testGenerateAndValidateToken() {
        String token = jwtService.generateAccessToken(principal);

        assertEquals("komorebi_user", jwtService.extractUsername(token));
        assertEquals("USER", jwtService.extractRole(token));
        assertTrue(jwtService.isTokenValid(token, principal));
    }

    @Test
    @DisplayName("Should reject token when username does not match or token is expired")
    void testInvalidAndExpiredToken() {
        String token = jwtService.generateAccessToken(principal);
        UserPrincipal otherUser = new UserPrincipal(
                2L,
                "other_user",
                "other@komorebi.jp",
                "encoded",
                Role.ADMIN,
                true
        );
        assertFalse(jwtService.isTokenValid(token, otherUser));

        JwtProperties expiredProperties = new JwtProperties();
        expiredProperties.setSecret(SECRET);
        expiredProperties.setExpirationMs(-1000L);
        JwtService expiredJwtService = new JwtService(expiredProperties);
        String expiredToken = expiredJwtService.generateAccessToken(principal);

        assertThrows(JwtException.class, () -> jwtService.isTokenValid(expiredToken, principal));
    }

    @Test
    @DisplayName("Should extract bearer token cleanly from Authorization header")
    void testExtractBearerToken() {
        assertEquals(Optional.of("abc.def.ghi"), jwtService.extractBearerToken("Bearer abc.def.ghi"));
        assertEquals(Optional.empty(), jwtService.extractBearerToken("Basic abc"));
        assertEquals(Optional.empty(), jwtService.extractBearerToken(null));
    }
}

