package com.interviewsimulator.dto;

public record AuthResponse(String token, String email, String displayName, String role) {
}
