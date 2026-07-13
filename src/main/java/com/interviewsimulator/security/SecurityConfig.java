package com.interviewsimulator.security;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthRateLimitFilter authRateLimitFilter;
    private final CookieOAuth2AuthorizationRequestRepository authorizationRequestRepository;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    // Only present once GOOGLE_CLIENT_ID/GOOGLE_CLIENT_SECRET are configured — see GoogleOAuthClientConfig.
    private final ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, AuthRateLimitFilter authRateLimitFilter,
                           CookieOAuth2AuthorizationRequestRepository authorizationRequestRepository,
                           OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler,
                           ObjectProvider<ClientRegistrationRepository> clientRegistrationRepositoryProvider) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authRateLimitFilter = authRateLimitFilter;
        this.authorizationRequestRepository = authorizationRequestRepository;
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
        this.clientRegistrationRepositoryProvider = clientRegistrationRepositoryProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
                        .requestMatchers("/", "/index.html", "/assets/**", "/login", "/admin", "/error",
                                "/forgot-password", "/reset-password", "/oauth-callback").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(authRateLimitFilter, JwtAuthenticationFilter.class);

        // Google OAuth login yalnız real credentials (GOOGLE_CLIENT_ID/SECRET) təyin olunduqda aktivləşir.
        if (clientRegistrationRepositoryProvider.getIfAvailable() != null) {
            http.oauth2Login(oauth2 -> oauth2
                    .authorizationEndpoint(a -> a.authorizationRequestRepository(authorizationRequestRepository))
                    .successHandler(oAuth2LoginSuccessHandler)
                    .failureHandler((req, res, ex) -> res.sendRedirect("/login?error=oauth")));
        }

        return http.build();
    }
}
