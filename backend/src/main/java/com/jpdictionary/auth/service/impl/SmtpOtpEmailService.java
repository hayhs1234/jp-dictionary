package com.jpdictionary.auth.service.impl;

import com.jpdictionary.auth.config.RegistrationProperties;
import com.jpdictionary.auth.service.OtpEmailService;
import com.jpdictionary.common.exception.EmailDeliveryException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmtpOtpEmailService implements OtpEmailService {

    private static final String SUBJECT = "[Komorebi] Mã OTP xác thực đăng ký tài khoản";

    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final RegistrationProperties registrationProperties;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Override
    public void sendRegistrationOtp(String toEmail, String displayName, String otpCode, long ttlMinutes) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new EmailDeliveryException("Dịch vụ gửi email hiện chưa khả dụng.");
        }
        validateMailCredentials();

        String fromAddress = resolveFromAddress();
        String fromName = registrationProperties.getMail().getFromName();
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage, true, StandardCharsets.UTF_8.name());
            helper.setFrom(fromAddress, fromName);
            helper.setTo(toEmail);
            helper.setSubject(SUBJECT);
            helper.setText(
                    buildPlainTextBody(displayName, otpCode, ttlMinutes),
                    buildHtmlBody(displayName, otpCode, ttlMinutes)
            );
            mailSender.send(mimeMessage);
            log.info("Sent registration OTP email to {}", toEmail);
        } catch (MailException | MessagingException | UnsupportedEncodingException ex) {
            log.error("Failed to send registration OTP email to {}: {}", toEmail, ex.getMessage(), ex);
            throw new EmailDeliveryException(
                    "Không thể gửi email xác thực OTP qua Gmail SMTP. Vui lòng kiểm tra cấu hình hoặc thử lại sau.",
                    ex
            );
        }
    }

    private void validateMailCredentials() {
        if (isBlank(mailUsername) || isBlank(mailPassword)) {
            throw new EmailDeliveryException(
                    "Hệ thống chưa cấu hình Gmail SMTP (MAIL_USERNAME / MAIL_PASSWORD).");
        }
    }

    private String resolveFromAddress() {
        String configuredFrom = registrationProperties.getMail().getFromAddress();
        return isBlank(configuredFrom) ? mailUsername.trim() : configuredFrom.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String buildPlainTextBody(String displayName, String otpCode, long ttlMinutes) {
        return "Xin chào " + displayName + ",\n\n"
                + "Mã OTP xác thực đăng ký tài khoản Komorebi Nihongo Hub của bạn là: " + otpCode + "\n"
                + "Mã có hiệu lực trong " + ttlMinutes + " phút.\n\n"
                + "Vui lòng không chia sẻ mã này cho bất kỳ ai.\n"
                + "Nếu bạn không thực hiện yêu cầu này, hãy bỏ qua email này.\n";
    }

    private String buildHtmlBody(String displayName, String otpCode, long ttlMinutes) {
        String safeName = HtmlUtils.htmlEscape(displayName);
        String safeOtp = HtmlUtils.htmlEscape(otpCode);
        return "<div style=\"font-family:'Segoe UI',Arial,sans-serif;max-width:540px;margin:0 auto;"
                + "padding:24px;background:#f6fbf5;border:1px solid #e2beba;border-radius:12px;\">"
                + "<div style=\"border-bottom:2px solid #990f12;padding-bottom:12px;margin-bottom:20px;\">"
                + "<h2 style=\"margin:0;color:#990f12;font-size:20px;\">Komorebi (木漏れ日) Nihongo Hub</h2>"
                + "<p style=\"margin:4px 0 0;color:#5a403d;font-size:13px;\">Xác thực đăng ký tài khoản mới</p>"
                + "</div>"
                + "<p style=\"color:#181d1a;font-size:15px;\">Xin chào <strong>" + safeName + "</strong>,</p>"
                + "<p style=\"color:#181d1a;font-size:15px;line-height:1.5;\">"
                + "Cảm ơn bạn đã đăng ký tài khoản tại Komorebi. "
                + "Vui lòng nhập mã OTP dưới đây để hoàn tất xác thực email:</p>"
                + "<div style=\"margin:24px 0;padding:18px;text-align:center;background:#ffffff;"
                + "border:1px dashed #990f12;border-radius:10px;\">"
                + "<span style=\"font-size:32px;font-weight:700;letter-spacing:8px;color:#990f12;\">"
                + safeOtp + "</span>"
                + "</div>"
                + "<p style=\"color:#5a403d;font-size:13px;line-height:1.5;\">"
                + "Mã OTP có hiệu lực trong <strong>" + ttlMinutes + " phút</strong>. "
                + "Tuyệt đối không chia sẻ mã này cho bất kỳ ai.</p>"
                + "<p style=\"color:#8e706c;font-size:12px;margin-top:20px;\">"
                + "Nếu bạn không thực hiện yêu cầu đăng ký này, vui lòng bỏ qua email.</p>"
                + "</div>";
    }
}

