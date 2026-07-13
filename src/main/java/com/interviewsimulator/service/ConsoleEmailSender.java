package com.interviewsimulator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Default {@link EmailSender}: logs the email instead of sending it. Swap in a real
 * SMTP-backed implementation (spring-boot-starter-mail + JavaMailSender) once production
 * mail credentials are available — no other code needs to change.
 */
@Service
public class ConsoleEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(ConsoleEmailSender.class);

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        log.info("[EMAIL] Şifrə bərpası linki {} ünvanına: {}", toEmail, resetLink);
    }
}
