package com.jpdictionary.auth.service.impl;

import com.jpdictionary.auth.config.RegistrationProperties;
import com.jpdictionary.common.exception.EmailDeliveryException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SmtpOtpEmailServiceTest {

    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    @Mock
    private JavaMailSender javaMailSender;

    private RegistrationProperties registrationProperties;
    private SmtpOtpEmailService smtpOtpEmailService;

    @BeforeEach
    void setUp() {
        registrationProperties = new RegistrationProperties();
        registrationProperties.getMail().setFromAddress("sender@gmail.com");
        registrationProperties.getMail().setFromName("Komorebi Hub");

        smtpOtpEmailService = new SmtpOtpEmailService(mailSenderProvider, registrationProperties);
        ReflectionTestUtils.setField(smtpOtpEmailService, "mailUsername", "sender@gmail.com");
        ReflectionTestUtils.setField(smtpOtpEmailService, "mailPassword", "app-password-1234");
    }

    @Test
    @DisplayName("Should build and send UTF-8 MimeMessage with OTP code")
    void testSendRegistrationOtpSuccess() throws Exception {
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSenderProvider.getIfAvailable()).thenReturn(javaMailSender);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        smtpOtpEmailService.sendRegistrationOtp("user@gmail.com", "Minh Anh", "654321", 5L);

        verify(javaMailSender).send(mimeMessage);
        assertEquals("[Komorebi] Mã OTP xác thực đăng ký tài khoản", mimeMessage.getSubject());
        assertTrue(mimeMessage.getAllRecipients()[0].toString().contains("user@gmail.com"));
    }

    @Test
    @DisplayName("Should throw EmailDeliveryException when SMTP credentials missing or send fails")
    void testSendRegistrationOtpFailures() {
        when(mailSenderProvider.getIfAvailable()).thenReturn(null);
        assertThrows(EmailDeliveryException.class,
                () -> smtpOtpEmailService.sendRegistrationOtp("user@gmail.com", "Minh", "123456", 5L));

        when(mailSenderProvider.getIfAvailable()).thenReturn(javaMailSender);
        ReflectionTestUtils.setField(smtpOtpEmailService, "mailPassword", "");
        assertThrows(EmailDeliveryException.class,
                () -> smtpOtpEmailService.sendRegistrationOtp("user@gmail.com", "Minh", "123456", 5L));

        ReflectionTestUtils.setField(smtpOtpEmailService, "mailPassword", "app-password");
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("SMTP error")).when(javaMailSender).send(mimeMessage);

        assertThrows(EmailDeliveryException.class,
                () -> smtpOtpEmailService.sendRegistrationOtp("user@gmail.com", "Minh", "123456", 5L));
    }
}

