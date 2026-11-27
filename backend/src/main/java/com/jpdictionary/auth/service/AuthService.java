package com.jpdictionary.auth.service;

import com.jpdictionary.auth.dto.AuthResponse;
import com.jpdictionary.auth.dto.LoginRequest;
import com.jpdictionary.auth.dto.UpdateProfileRequest;
import com.jpdictionary.auth.dto.UserProfileDto;

import java.util.List;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    UserProfileDto getCurrentUser(String identifier);

    UserProfileDto updateProfile(String identifier, UpdateProfileRequest request);

    void logout(String authorizationHeader);

    List<UserProfileDto> listAllUsers();
}

