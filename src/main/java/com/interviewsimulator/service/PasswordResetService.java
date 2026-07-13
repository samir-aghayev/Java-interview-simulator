package com.interviewsimulator.service;

import com.interviewsimulator.entity.PasswordResetTokenEntity;
import com.interviewsimulator.entity.UserEntity;
import com.interviewsimulator.repository.PasswordResetTokenRepository;
import com.interviewsimulator.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class PasswordResetService {

    private static final long TOKEN_VALIDITY_MINUTES = 30;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;
    private final String frontendUrl;
    private final SecureRandom random = new SecureRandom();

    public PasswordResetService(UserRepository userRepository, PasswordResetTokenRepository tokenRepository,
                                 PasswordEncoder passwordEncoder, EmailSender emailSender,
                                 @Value("${app.frontend-url}") String frontendUrl) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailSender = emailSender;
        this.frontendUrl = frontendUrl;
    }

    /** İstifadəçi mövcud olmasa belə eyni cavab qaytarılır ki, email siyahısı sızdırılmasın. */
    @Transactional
    public void requestReset(String email) {
        String normalized = email.trim().toLowerCase();
        userRepository.findByEmail(normalized).ifPresent(user -> {
            String token = generateToken();
            tokenRepository.save(new PasswordResetTokenEntity(user, token,
                    LocalDateTime.now().plusMinutes(TOKEN_VALIDITY_MINUTES)));
            String resetLink = frontendUrl + "/reset-password?token=" + token;
            emailSender.sendPasswordResetEmail(user.getEmail(), resetLink);
        });
    }

    @Transactional
    public UserEntity resetPassword(String token, String newPassword) {
        PasswordResetTokenEntity resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link etibarsızdır və ya vaxtı keçib"));
        if (!resetToken.isValid()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link etibarsızdır və ya vaxtı keçib");
        }
        UserEntity user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        resetToken.markUsed();
        tokenRepository.save(resetToken);
        return user;
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
