package com.interviewsimulator.security;

import com.interviewsimulator.entity.UserEntity;
import com.interviewsimulator.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final String DEFAULT_ROLE = "USER";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final String frontendUrl;

    public OAuth2LoginSuccessHandler(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                      JwtService jwtService, @Value("${app.frontend-url}") String frontendUrl) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        if (email == null || email.isBlank()) {
            response.sendRedirect(frontendUrl + "/login?error=oauth");
            return;
        }
        String normalizedEmail = email.trim().toLowerCase();
        String name = oAuth2User.getAttribute("name");
        String displayName = (name == null || name.isBlank()) ? normalizedEmail : name;

        UserEntity user = userRepository.findByEmail(normalizedEmail)
                .orElseGet(() -> userRepository.save(new UserEntity(normalizedEmail,
                        passwordEncoder.encode(UUID.randomUUID().toString()), displayName, DEFAULT_ROLE)));

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        response.sendRedirect(frontendUrl + "/oauth-callback?token=" + encodedToken);
    }
}
