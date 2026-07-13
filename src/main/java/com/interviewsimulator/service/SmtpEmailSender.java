package com.interviewsimulator.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class SmtpEmailSender implements EmailSender {

    private static final Logger log = LoggerFactory.getLogger(SmtpEmailSender.class);

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpEmailSender(JavaMailSender mailSender, String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(toEmail);
        message.setSubject("Şifrə bərpası");
        message.setText("Şifrənizi bərpa etmək üçün linkə keçin:\n\n" + resetLink
                + "\n\nBu link 30 dəqiqə etibarlıdır. Bu tələbi siz etməmisinizsə, mesajı gözardı edin.");
        try {
            mailSender.send(message);
        } catch (MailException e) {
            // Reset token artıq yaradılıb — SMTP-nin müvəqqəti əlçatmazlığı istifadəçini
            // xəta ilə qarşılamamalı və ya email-in mövcud olub-olmadığını sızdırmamalıdır.
            log.error("Şifrə bərpası email-i göndərilə bilmədi ({}): {}", toEmail, e.getMessage());
        }
    }
}
