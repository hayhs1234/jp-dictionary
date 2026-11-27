package com.jpdictionary.auth.controller;

import com.jpdictionary.auth.dto.UserProfileDto;
import com.jpdictionary.auth.service.AuthService;
import com.jpdictionary.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AuthService authService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserProfileDto>>> listUsers() {
        return ResponseEntity.ok(ApiResponse.success(authService.listAllUsers()));
    }
}

