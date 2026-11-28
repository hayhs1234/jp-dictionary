package com.jpdictionary.auth.service;

public interface OtpEmailService {

    void sendRegistrationOtp(String toEmail, String displayName, String otpCode, long ttlMinutes);
}

