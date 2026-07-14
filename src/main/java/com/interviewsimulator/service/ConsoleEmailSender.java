package com.interviewsimulator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fallback {@link EmailSender}: logs the email instead of sending it. Used automatically
 * whenever no SMTP host is configured — see {@link com.interviewsimulator.config.EmailConfig}.
 */
public class ConsoleEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(ConsoleEmailSender.class);

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink, String locale) {
        PasswordResetEmailContent content = PasswordResetEmailContent.forLocale(locale, resetLink);
        log.info("[EMAIL] {} ({}) -> {}: {}", content.subject, locale, toEmail, resetLink);
    }
}
