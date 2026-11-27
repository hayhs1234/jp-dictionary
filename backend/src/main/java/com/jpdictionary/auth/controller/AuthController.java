package com.jpdictionary.auth.controller;

import com.jpdictionary.auth.dto.AuthResponse;
import com.jpdictionary.auth.dto.LoginRequest;
import com.jpdictionary.auth.dto.UpdateProfileRequest;
import com.jpdictionary.auth.dto.UserProfileDto;
import com.jpdictionary.auth.service.AuthService;
import com.jpdictionary.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Đăng nhập thành công", response));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileDto>> getCurrentUser(Authentication authentication) {
        UserProfileDto profile = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PatchMapping("/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UserProfileDto updated = authService.updateProfile(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin cá nhân thành công", updated));
    }

    @PostMapping("/logout")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader
    ) {
        authService.logout(authorizationHeader);
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công", null));
    }
}

