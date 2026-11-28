package com.jpdictionary.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.registration")
@Getter
@Setter
public class RegistrationProperties {

    private int otpLength = 6;
    private long otpExpirationSeconds = 300L;
    private int maxAttempts = 5;
    private long resendCooldownSeconds = 60L;
    private int maxResends = 5;
    private Mail mail = new Mail();

    @Getter
    @Setter
    public static class Mail {
        private String fromAddress = "";
        private String fromName = "Komorebi Nihongo Hub";
    }
}

