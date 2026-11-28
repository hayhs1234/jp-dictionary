package com.jpdictionary.auth.service.impl;

import com.jpdictionary.auth.config.RegistrationProperties;
import com.jpdictionary.auth.dto.AuthResponse;
import com.jpdictionary.auth.dto.OtpDispatchResponse;
import com.jpdictionary.auth.dto.RegisterRequest;
import com.jpdictionary.auth.dto.ResendOtpRequest;
import com.jpdictionary.auth.dto.UserProfileDto;
import com.jpdictionary.auth.dto.VerifyOtpRequest;
import com.jpdictionary.auth.entity.PendingRegistration;
import com.jpdictionary.auth.entity.User;
import com.jpdictionary.auth.enums.Role;
import com.jpdictionary.auth.repository.PendingRegistrationRepository;
import com.jpdictionary.auth.repository.UserRepository;
import com.jpdictionary.auth.security.JwtProperties;
import com.jpdictionary.auth.security.JwtService;
import com.jpdictionary.auth.security.UserPrincipal;
import com.jpdictionary.auth.service.OtpEmailService;
import com.jpdictionary.auth.service.RegistrationService;
import com.jpdictionary.common.exception.DuplicateResourceException;
import com.jpdictionary.common.exception.InvalidOtpException;
import com.jpdictionary.common.exception.RateLimitExceededException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private static final String DEFAULT_JLPT_TARGET = "N5";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpEmailService otpEmailService;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final RegistrationProperties registrationProperties;

    @Override
    @Transactional
    public OtpDispatchResponse requestRegistrationOtp(RegisterRequest request) {
        String email = normalizeEmail(request.getEmail());
        String username = request.getUsername().trim();
        String displayName = request.getDisplayName().trim();
        String jlptTarget = normalizeJlptTarget(request.getJlptTarget());
        LocalDateTime now = LocalDateTime.now();

        pendingRegistrationRepository.deleteByExpiresAtBefore(now);
        ensureUserNotExists(email, username);

        PendingRegistration pending = pendingRegistrationRepository.findByEmailIgnoreCase(email)
                .orElseGet(PendingRegistration::new);

        int nextResendCount = resolveNextResendCount(pending, now);
        String rawOtp = generateNumericOtp(registrationProperties.getOtpLength());
        applyPendingRegistrationData(
                pending,
                email,
                username,
                passwordEncoder.encode(request.getPassword()),
                displayName,
                jlptTarget,
                passwordEncoder.encode(rawOtp),
                now,
                nextResendCount
        );
        pendingRegistrationRepository.save(pending);

        sendOtpEmail(email, displayName, rawOtp);
        return buildDispatchResponse(email);
    }

    @Override
    @Transactional
    public OtpDispatchResponse resendOtp(ResendOtpRequest request) {
        String email = normalizeEmail(request.getEmail());
        PendingRegistration pending = pendingRegistrationRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new InvalidOtpException(
                        "Không tìm thấy yêu cầu đăng ký cho email này. Vui lòng đăng ký lại."));

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Email này đã được đăng ký tài khoản");
        }

        LocalDateTime now = LocalDateTime.now();
        int nextResendCount = resolveNextResendCount(pending, now);
        String rawOtp = generateNumericOtp(registrationProperties.getOtpLength());

        pending.setOtpHash(passwordEncoder.encode(rawOtp));
        pending.setExpiresAt(now.plusSeconds(registrationProperties.getOtpExpirationSeconds()));
        pending.setLastSentAt(now);
        pending.setAttemptCount(0);
        pending.setResendCount(nextResendCount);
        pendingRegistrationRepository.save(pending);

        sendOtpEmail(email, pending.getDisplayName(), rawOtp);
        return buildDispatchResponse(email);
    }

    @Override
    @Transactional(noRollbackFor = InvalidOtpException.class)
    public AuthResponse verifyOtpAndCreateUser(VerifyOtpRequest request) {
        String email = normalizeEmail(request.getEmail());
        String otp = request.getOtp().trim();
        PendingRegistration pending = pendingRegistrationRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new InvalidOtpException(
                        "Không tìm thấy phiên xác thực cho email này. Vui lòng đăng ký lại."));

        LocalDateTime now = LocalDateTime.now();
        validateOtpUsable(pending, now);
        verifyOtpCodeOrIncrementAttempts(pending, otp);
        ensureUserNotExists(pending.getEmail(), pending.getUsername());

        User createdUser = userRepository.save(User.builder()
                .username(pending.getUsername())
                .email(pending.getEmail())
                .passwordHash(pending.getPasswordHash())
                .displayName(pending.getDisplayName())
                .jlptTarget(pending.getJlptTarget())
                .role(Role.USER)
                .active(true)
                .build());

        pendingRegistrationRepository.delete(pending);

        String accessToken = jwtService.generateAccessToken(UserPrincipal.from(createdUser));
        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getExpirationMs())
                .user(UserProfileDto.from(createdUser))
                .build();
    }

    private void ensureUserNotExists(String email, String username) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new DuplicateResourceException("Email này đã được đăng ký tài khoản");
        }
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new DuplicateResourceException("Tên đăng nhập này đã được sử dụng");
        }
    }

    private int resolveNextResendCount(PendingRegistration pending, LocalDateTime now) {
        if (pending.getId() == null) {
            return 0;
        }
        long elapsedSeconds = Duration.between(pending.getLastSentAt(), now).getSeconds();
        long cooldownSeconds = registrationProperties.getResendCooldownSeconds();
        if (elapsedSeconds < cooldownSeconds) {
            long waitSeconds = Math.max(1L, cooldownSeconds - elapsedSeconds);
            throw new RateLimitExceededException(
                    "Vui lòng đợi " + waitSeconds + " giây trước khi gửi lại mã OTP.");
        }
        int nextCount = pending.getResendCount() + 1;
        if (nextCount > registrationProperties.getMaxResends()) {
            throw new RateLimitExceededException(
                    "Bạn đã vượt quá số lần gửi lại mã OTP cho phép. Vui lòng thử lại sau.");
        }
        return nextCount;
    }

    private void applyPendingRegistrationData(
            PendingRegistration pending,
            String email,
            String username,
            String passwordHash,
            String displayName,
            String jlptTarget,
            String otpHash,
            LocalDateTime now,
            int resendCount
    ) {
        pending.setEmail(email);
        pending.setUsername(username);
        pending.setPasswordHash(passwordHash);
        pending.setDisplayName(displayName);
        pending.setJlptTarget(jlptTarget);
        pending.setOtpHash(otpHash);
        pending.setExpiresAt(now.plusSeconds(registrationProperties.getOtpExpirationSeconds()));
        pending.setLastSentAt(now);
        pending.setAttemptCount(0);
        pending.setResendCount(resendCount);
    }

    private void validateOtpUsable(PendingRegistration pending, LocalDateTime now) {
        if (pending.getExpiresAt().isBefore(now)) {
            throw new InvalidOtpException("Mã OTP đã hết hạn. Vui lòng yêu cầu gửi lại mã mới.");
        }
        if (pending.getAttemptCount() >= registrationProperties.getMaxAttempts()) {
            throw new InvalidOtpException(
                    "Bạn đã nhập sai OTP quá số lần cho phép. Vui lòng yêu cầu gửi lại mã mới.");
        }
    }

    private void verifyOtpCodeOrIncrementAttempts(PendingRegistration pending, String otp) {
        if (passwordEncoder.matches(otp, pending.getOtpHash())) {
            return;
        }
        int updatedAttempts = pending.getAttemptCount() + 1;
        pending.setAttemptCount(updatedAttempts);
        pendingRegistrationRepository.save(pending);

        int remaining = Math.max(0, registrationProperties.getMaxAttempts() - updatedAttempts);
        if (remaining == 0) {
            throw new InvalidOtpException(
                    "Mã OTP không chính xác. Bạn đã hết lượt thử, vui lòng yêu cầu gửi lại mã mới.");
        }
        throw new InvalidOtpException(
                "Mã OTP không chính xác. Bạn còn " + remaining + " lần thử.");
    }

    private void sendOtpEmail(String email, String displayName, String otpCode) {
        long ttlMinutes = Math.max(1L, registrationProperties.getOtpExpirationSeconds() / 60L);
        otpEmailService.sendRegistrationOtp(email, displayName, otpCode, ttlMinutes);
    }

    private OtpDispatchResponse buildDispatchResponse(String email) {
        return OtpDispatchResponse.builder()
                .email(email)
                .expiresInSeconds(registrationProperties.getOtpExpirationSeconds())
                .resendCooldownSeconds(registrationProperties.getResendCooldownSeconds())
                .build();
    }

    private String generateNumericOtp(int length) {
        int digits = Math.max(4, length);
        StringBuilder sb = new StringBuilder(digits);
        for (int i = 0; i < digits; i++) {
            sb.append(SECURE_RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeJlptTarget(String jlptTarget) {
        if (jlptTarget == null || jlptTarget.trim().isEmpty()) {
            return DEFAULT_JLPT_TARGET;
        }
        return jlptTarget.trim().toUpperCase(Locale.ROOT);
    }
}

