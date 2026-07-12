package com.interviewsimulator.security;

import com.interviewsimulator.entity.UserEntity;
import com.interviewsimulator.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminBootstrap(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           @Value("${app.admin.email}") String adminEmail,
                           @Value("${app.admin.password}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminEmail == null || adminEmail.isBlank()) {
            return;
        }
        String email = adminEmail.trim().toLowerCase();
        userRepository.findByEmail(email).ifPresentOrElse(user -> {
            if (!"ADMIN".equals(user.getRole())) {
                user.setRole("ADMIN");
                userRepository.save(user);
                log.info("Admin bootstrap: {} ADMIN roluna yüksəldildi", email);
            }
        }, () -> {
            if (adminPassword == null || adminPassword.isBlank()) {
                log.warn("Admin bootstrap: {} mövcud deyil və ADMIN_PASSWORD verilməyib — admin yaradılmadı", email);
                return;
            }
            userRepository.save(new UserEntity(email, passwordEncoder.encode(adminPassword), "Admin", "ADMIN"));
            log.info("Admin bootstrap: {} ADMIN olaraq yaradıldı", email);
        });
    }
}
