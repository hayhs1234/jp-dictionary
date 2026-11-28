package com.jpdictionary.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpDispatchResponse {

    private String email;
    private long expiresInSeconds;
    private long resendCooldownSeconds;
}

