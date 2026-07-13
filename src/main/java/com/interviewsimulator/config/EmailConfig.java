package com.interviewsimulator.config;

import com.interviewsimulator.service.ConsoleEmailSender;
import com.interviewsimulator.service.EmailSender;
import com.interviewsimulator.service.SmtpEmailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

/**
 * Picks the {@link EmailSender} implementation with a plain if/else in one {@code @Bean}
 * method — deterministic, no risk of two beans matching or condition-ordering surprises.
 * No SMTP_HOST configured → falls back to logging (matches plain `gradlew bootRun` with no
 * env vars); host configured → real SMTP. Auth is optional (Mailpit/local relays don't need
 * it; most real providers do), so username/password are only applied when both are set.
 */
@Configuration
public class EmailConfig {

    @Bean
    public EmailSender emailSender(@Value("${app.smtp.host:}") String host,
                                    @Value("${app.smtp.port:587}") int port,
                                    @Value("${app.smtp.username:}") String username,
                                    @Value("${app.smtp.password:}") String password,
                                    @Value("${app.mail.from}") String from) {
        if (host.isBlank()) {
            return new ConsoleEmailSender();
        }

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);

        boolean authenticated = !username.isBlank();
        if (authenticated) {
            mailSender.setUsername(username);
            mailSender.setPassword(password);
        }

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", String.valueOf(authenticated));
        props.put("mail.smtp.starttls.enable", String.valueOf(authenticated));

        return new SmtpEmailSender(mailSender, from);
    }
}
