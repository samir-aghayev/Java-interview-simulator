package com.interviewsimulator.web;

import com.interviewsimulator.dto.AuthResponse;
import com.interviewsimulator.dto.LoginRequest;
import com.interviewsimulator.dto.RegisterRequest;
import com.interviewsimulator.entity.UserEntity;
import com.interviewsimulator.repository.UserRepository;
import com.interviewsimulator.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AuthController {

    private static final String DEFAULT_ROLE = "USER";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    private static final java.util.Set<String> GENDERS = java.util.Set.of("MALE", "FEMALE");
    private static final java.util.Set<String> EMPLOYMENT_STATUSES =
            java.util.Set.of("STUDENT", "EMPLOYED", "UNEMPLOYED", "FREELANCER", "OTHER");
    private static final java.util.Set<String> EDUCATION_STATUSES =
            java.util.Set.of("HIGH_SCHOOL", "BACHELOR", "MASTER", "PHD", "OTHER");

    @PostMapping("/api/auth/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bu email artıq qeydiyyatdan keçib");
        }
        String gender = optionalChoice(request.gender(), GENDERS, "gender");
        String employment = optionalChoice(request.employmentStatus(), EMPLOYMENT_STATUSES, "employmentStatus");
        String education = optionalChoice(request.educationStatus(), EDUCATION_STATUSES, "educationStatus");

        String firstName = request.firstName().trim();
        String lastName = request.lastName().trim();
        UserEntity user = new UserEntity(email, passwordEncoder.encode(request.password()),
                firstName + " " + lastName, DEFAULT_ROLE);
        user.setProfile(firstName, lastName, request.birthDate(), gender,
                request.country() == null || request.country().isBlank() ? null : request.country().trim(),
                employment, education);
        userRepository.save(user);
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
        return new AuthResponse(token, user.getEmail(), user.getDisplayName(), user.getRole());
    }

    private String optionalChoice(String value, java.util.Set<String> allowed, String fieldName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toUpperCase();
        if (!allowed.contains(normalized)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " üçün yanlış dəyər");
        }
        return normalized;
    }

    @PostMapping("/api/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Yanlış email və ya şifrə"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Yanlış email və ya şifrə");
        }
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());
        return new AuthResponse(token, user.getEmail(), user.getDisplayName(), user.getRole());
    }
}
