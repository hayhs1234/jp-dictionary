package com.jpdictionary.auth.service.impl;

import com.jpdictionary.auth.dto.AuthResponse;
import com.jpdictionary.auth.dto.LoginRequest;
import com.jpdictionary.auth.dto.UpdateProfileRequest;
import com.jpdictionary.auth.dto.UserProfileDto;
import com.jpdictionary.auth.entity.User;
import com.jpdictionary.auth.repository.UserRepository;
import com.jpdictionary.auth.security.JwtProperties;
import com.jpdictionary.auth.security.JwtService;
import com.jpdictionary.auth.security.TokenRevocationService;
import com.jpdictionary.auth.security.UserPrincipal;
import com.jpdictionary.auth.service.AuthService;
import com.jpdictionary.common.exception.ResourceNotFoundException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final TokenRevocationService tokenRevocationService;

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String cleanIdentifier = request.getIdentifier().trim();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(cleanIdentifier, request.getPassword())
        );

        User user = findUserByIdentifier(cleanIdentifier);
        String accessToken = jwtService.generateAccessToken(UserPrincipal.from(user));

        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getExpirationMs())
                .user(UserProfileDto.from(user))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserProfileDto getCurrentUser(String identifier) {
        return UserProfileDto.from(findUserByIdentifier(identifier));
    }

    @Override
    @Transactional
    public UserProfileDto updateProfile(String identifier, UpdateProfileRequest request) {
        User user = findUserByIdentifier(identifier);
        user.setDisplayName(request.getDisplayName().trim());
        if (request.getJlptTarget() != null && !request.getJlptTarget().isBlank()) {
            user.setJlptTarget(request.getJlptTarget().trim().toUpperCase());
        }
        if (request.getBio() != null) {
            String trimmedBio = request.getBio().trim();
            user.setBio(trimmedBio.isEmpty() ? null : trimmedBio);
        }
        return UserProfileDto.from(userRepository.save(user));
    }

    @Override
    public void logout(String authorizationHeader) {
        jwtService.extractBearerToken(authorizationHeader).ifPresent(this::revokeTokenSafely);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfileDto> listAllUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(UserProfileDto::from)
                .toList();
    }

    private User findUserByIdentifier(String identifier) {
        String clean = identifier == null ? "" : identifier.trim();
        return userRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase(clean, clean)
                .orElseThrow(() -> new ResourceNotFoundException("User", "identifier", clean));
    }

    private void revokeTokenSafely(String token) {
        try {
            Instant expiresAt = jwtService.extractExpiration(token);
            tokenRevocationService.revoke(token, expiresAt);
        } catch (JwtException | IllegalArgumentException ignored) {
            // Ignore malformed/already expired tokens on logout
        }
    }
}

