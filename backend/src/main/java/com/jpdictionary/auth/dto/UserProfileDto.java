package com.jpdictionary.auth.dto;

import com.jpdictionary.auth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {

    private Long id;
    private String username;
    private String email;
    private String displayName;
    private String avatarUrl;
    private String jlptTarget;
    private String bio;
    private String role;
    private boolean active;
    private LocalDateTime createdAt;

    public static UserProfileDto from(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .jlptTarget(user.getJlptTarget())
                .bio(user.getBio())
                .role(user.getRole().name())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}

