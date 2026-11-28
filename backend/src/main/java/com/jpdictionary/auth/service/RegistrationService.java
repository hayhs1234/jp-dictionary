package com.jpdictionary.auth.service;

import com.jpdictionary.auth.dto.AuthResponse;
import com.jpdictionary.auth.dto.OtpDispatchResponse;
import com.jpdictionary.auth.dto.RegisterRequest;
import com.jpdictionary.auth.dto.ResendOtpRequest;
import com.jpdictionary.auth.dto.VerifyOtpRequest;

public interface RegistrationService {

    OtpDispatchResponse requestRegistrationOtp(RegisterRequest request);

    OtpDispatchResponse resendOtp(ResendOtpRequest request);

    AuthResponse verifyOtpAndCreateUser(VerifyOtpRequest request);
}

