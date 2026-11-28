package com.jpdictionary.auth.service.impl;

import com.jpdictionary.auth.config.RegistrationProperties;
import com.jpdictionary.auth.dto.AuthResponse;
import com.jpdictionary.auth.dto.OtpDispatchResponse;
import com.jpdictionary.auth.dto.RegisterRequest;
import com.jpdictionary.auth.dto.ResendOtpRequest;
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
import com.jpdictionary.common.exception.DuplicateResourceException;
import com.jpdictionary.common.exception.InvalidOtpException;
import com.jpdictionary.common.exception.RateLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PendingRegistrationRepository pendingRegistrationRepository;

    @Mock
    private OtpEmailService otpEmailService;

    @Mock
    private JwtService jwtService;

    private PasswordEncoder passwordEncoder;
    private RegistrationProperties registrationProperties;
    private RegistrationServiceImpl registrationService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setExpirationMs(86400000L);

        registrationProperties = new RegistrationProperties();
        registrationProperties.setOtpLength(6);
        registrationProperties.setOtpExpirationSeconds(300L);
        registrationProperties.setMaxAttempts(5);
        registrationProperties.setResendCooldownSeconds(60L);
        registrationProperties.setMaxResends(5);

        registrationService = new RegistrationServiceImpl(
                userRepository,
                pendingRegistrationRepository,
                passwordEncoder,
                otpEmailService,
                jwtService,
                jwtProperties,
                registrationProperties
        );
    }

    @Test
    @DisplayName("Should store hashed pending registration and send 6-digit OTP email")
    void testRequestRegistrationOtpSuccess() {
        RegisterRequest request = new RegisterRequest(
                " sakura_dev ",
                " Sakura@Gmail.com ",
                "Secret123",
                " Sakura Chan ",
                "n3"
        );
        when(userRepository.existsByEmailIgnoreCase("sakura@gmail.com")).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase("sakura_dev")).thenReturn(false);
        when(pendingRegistrationRepository.findByEmailIgnoreCase("sakura@gmail.com"))
                .thenReturn(Optional.empty());
        when(pendingRegistrationRepository.save(any(PendingRegistration.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        OtpDispatchResponse response = registrationService.requestRegistrationOtp(request);

        assertEquals("sakura@gmail.com", response.getEmail());
        assertEquals(300L, response.getExpiresInSeconds());
        assertEquals(60L, response.getResendCooldownSeconds());

        ArgumentCaptor<PendingRegistration> pendingCaptor = ArgumentCaptor.forClass(PendingRegistration.class);
        verify(pendingRegistrationRepository).save(pendingCaptor.capture());
        PendingRegistration savedPending = pendingCaptor.getValue();

        assertEquals("sakura_dev", savedPending.getUsername());
        assertEquals("Sakura Chan", savedPending.getDisplayName());
        assertEquals("N3", savedPending.getJlptTarget());
        assertNotEquals("Secret123", savedPending.getPasswordHash());
        assertTrue(passwordEncoder.matches("Secret123", savedPending.getPasswordHash()));

        ArgumentCaptor<String> otpCaptor = ArgumentCaptor.forClass(String.class);
        verify(otpEmailService).sendRegistrationOtp(
                eq("sakura@gmail.com"),
                eq("Sakura Chan"),
                otpCaptor.capture(),
                eq(5L)
        );
        String sentOtp = otpCaptor.getValue();
        assertTrue(sentOtp.matches("^\\d{6}$"));
        assertTrue(passwordEncoder.matches(sentOtp, savedPending.getOtpHash()));
    }

    @Test
    @DisplayName("Should reject duplicate email or username before sending OTP")
    void testRequestRegistrationOtpRejectsDuplicate() {
        RegisterRequest request = new RegisterRequest(
                "komorebi_user", "user@komorebi.jp", "Secret123", "User", "N5");
        when(userRepository.existsByEmailIgnoreCase("user@komorebi.jp")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> registrationService.requestRegistrationOtp(request));
        verify(otpEmailService, never()).sendRegistrationOtp(any(), any(), any(), anyLong());

        when(userRepository.existsByEmailIgnoreCase("user@komorebi.jp")).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase("komorebi_user")).thenReturn(true);
        assertThrows(DuplicateResourceException.class,
                () -> registrationService.requestRegistrationOtp(request));
    }

    @Test
    @DisplayName("Should enforce resend cooldown and max resend limit")
    void testResendOtpCooldownAndLimit() {
        PendingRegistration recent = PendingRegistration.builder()
                .id(1L)
                .email("sakura@gmail.com")
                .username("sakura")
                .displayName("Sakura")
                .passwordHash("hash")
                .otpHash("otpHash")
                .expiresAt(LocalDateTime.now().plusMinutes(4))
                .lastSentAt(LocalDateTime.now().minusSeconds(20))
                .resendCount(0)
                .build();
        when(pendingRegistrationRepository.findByEmailIgnoreCase("sakura@gmail.com"))
                .thenReturn(Optional.of(recent));

        assertThrows(RateLimitExceededException.class,
                () -> registrationService.resendOtp(new ResendOtpRequest("sakura@gmail.com")));

        recent.setLastSentAt(LocalDateTime.now().minusSeconds(90));
        recent.setResendCount(5);
        assertThrows(RateLimitExceededException.class,
                () -> registrationService.resendOtp(new ResendOtpRequest("sakura@gmail.com")));

        recent.setResendCount(1);
        when(pendingRegistrationRepository.save(any(PendingRegistration.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        OtpDispatchResponse response = registrationService.resendOtp(
                new ResendOtpRequest("sakura@gmail.com"));

        assertEquals("sakura@gmail.com", response.getEmail());
        assertEquals(2, recent.getResendCount());
        assertEquals(0, recent.getAttemptCount());
    }

    @Test
    @DisplayName("Should verify OTP, create USER account, delete pending row, and return JWT")
    void testVerifyOtpSuccess() {
        PendingRegistration pending = PendingRegistration.builder()
                .id(1L)
                .email("sakura@gmail.com")
                .username("sakura_dev")
                .passwordHash(passwordEncoder.encode("Secret123"))
                .displayName("Sakura Chan")
                .jlptTarget("N4")
                .otpHash(passwordEncoder.encode("123456"))
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .lastSentAt(LocalDateTime.now())
                .attemptCount(0)
                .build();

        when(pendingRegistrationRepository.findByEmailIgnoreCase("sakura@gmail.com"))
                .thenReturn(Optional.of(pending));
        when(userRepository.existsByEmailIgnoreCase("sakura@gmail.com")).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase("sakura_dev")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(42L);
            return u;
        });
        when(jwtService.generateAccessToken(any(UserPrincipal.class))).thenReturn("new-jwt-token");

        AuthResponse auth = registrationService.verifyOtpAndCreateUser(
                new VerifyOtpRequest("sakura@gmail.com", "123456"));

        assertNotNull(auth);
        assertEquals("new-jwt-token", auth.getAccessToken());
        assertEquals("Bearer", auth.getTokenType());
        assertEquals("sakura_dev", auth.getUser().getUsername());
        assertEquals("N4", auth.getUser().getJlptTarget());
        assertEquals("USER", auth.getUser().getRole());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        assertEquals(Role.USER, userCaptor.getValue().getRole());
        assertTrue(userCaptor.getValue().isActive());

        verify(pendingRegistrationRepository).delete(pending);
    }

    @Test
    @DisplayName("Should increment attempt count on wrong OTP and reject expired or exhausted OTP")
    void testVerifyOtpWrongExpiredAndExhausted() {
        PendingRegistration pending = PendingRegistration.builder()
                .id(1L)
                .email("sakura@gmail.com")
                .username("sakura_dev")
                .passwordHash("hash")
                .displayName("Sakura")
                .jlptTarget("N5")
                .otpHash(passwordEncoder.encode("123456"))
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .lastSentAt(LocalDateTime.now())
                .attemptCount(4)
                .build();

        when(pendingRegistrationRepository.findByEmailIgnoreCase("sakura@gmail.com"))
                .thenReturn(Optional.of(pending));
        when(pendingRegistrationRepository.save(any(PendingRegistration.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        InvalidOtpException wrongEx = assertThrows(InvalidOtpException.class,
                () -> registrationService.verifyOtpAndCreateUser(
                        new VerifyOtpRequest("sakura@gmail.com", "000000")));
        assertTrue(wrongEx.getMessage().contains("hết lượt thử"));
        assertEquals(5, pending.getAttemptCount());

        assertThrows(InvalidOtpException.class,
                () -> registrationService.verifyOtpAndCreateUser(
                        new VerifyOtpRequest("sakura@gmail.com", "123456")));

        pending.setAttemptCount(0);
        pending.setExpiresAt(LocalDateTime.now().minusSeconds(1));
        InvalidOtpException expiredEx = assertThrows(InvalidOtpException.class,
                () -> registrationService.verifyOtpAndCreateUser(
                        new VerifyOtpRequest("sakura@gmail.com", "123456")));
        assertTrue(expiredEx.getMessage().contains("hết hạn"));
    }
}
