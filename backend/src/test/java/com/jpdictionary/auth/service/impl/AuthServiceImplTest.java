package com.jpdictionary.auth.service.impl;

import com.jpdictionary.auth.dto.AuthResponse;
import com.jpdictionary.auth.dto.LoginRequest;
import com.jpdictionary.auth.dto.UpdateProfileRequest;
import com.jpdictionary.auth.dto.UserProfileDto;
import com.jpdictionary.auth.entity.User;
import com.jpdictionary.auth.enums.Role;
import com.jpdictionary.auth.repository.UserRepository;
import com.jpdictionary.auth.security.JwtProperties;
import com.jpdictionary.auth.security.JwtService;
import com.jpdictionary.auth.security.TokenRevocationService;
import com.jpdictionary.auth.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenRevocationService tokenRevocationService;

    private AuthServiceImpl authService;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setExpirationMs(86400000L);
        authService = new AuthServiceImpl(
                authenticationManager,
                userRepository,
                jwtService,
                jwtProperties,
                tokenRevocationService
        );
        sampleUser = User.builder()
                .id(1L)
                .username("komorebi_user")
                .email("user@komorebi.jp")
                .passwordHash("hashed")
                .displayName("Minh Anh")
                .jlptTarget("N3")
                .role(Role.USER)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("Should login using either username or email and return AuthResponse")
    void testLoginWithUsernameOrEmail() {
        when(userRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase("user@komorebi.jp", "user@komorebi.jp"))
                .thenReturn(Optional.of(sampleUser));
        when(jwtService.generateAccessToken(any(UserPrincipal.class))).thenReturn("jwt-token");

        AuthResponse response = authService.login(new LoginRequest(" user@komorebi.jp ", "User@123"));

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken("user@komorebi.jp", "User@123")
        );
        assertEquals("jwt-token", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("komorebi_user", response.getUser().getUsername());
        assertEquals("USER", response.getUser().getRole());
    }

    @Test
    @DisplayName("Should update profile fields and preserve existing role")
    void testUpdateProfile() {
        when(userRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase("komorebi_user", "komorebi_user"))
                .thenReturn(Optional.of(sampleUser));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserProfileDto updated = authService.updateProfile(
                "komorebi_user",
                new UpdateProfileRequest("Minh Anh Updated", "n2", "Luyện thi N2")
        );

        assertEquals("Minh Anh Updated", updated.getDisplayName());
        assertEquals("N2", updated.getJlptTarget());
        assertEquals("Luyện thi N2", updated.getBio());
        assertEquals("USER", updated.getRole());
    }

    @Test
    @DisplayName("Should revoke bearer token on logout and list users sorted by id")
    void testLogoutAndListUsers() {
        Instant expiresAt = Instant.now().plusSeconds(600);
        when(jwtService.extractBearerToken("Bearer sample-jwt")).thenReturn(Optional.of("sample-jwt"));
        when(jwtService.extractExpiration("sample-jwt")).thenReturn(expiresAt);
        when(userRepository.findAll(any(Sort.class))).thenReturn(List.of(sampleUser));

        authService.logout("Bearer sample-jwt");
        verify(tokenRevocationService).revoke("sample-jwt", expiresAt);

        List<UserProfileDto> users = authService.listAllUsers();
        assertEquals(1, users.size());
        assertNotNull(users.get(0).getEmail());
    }
}

