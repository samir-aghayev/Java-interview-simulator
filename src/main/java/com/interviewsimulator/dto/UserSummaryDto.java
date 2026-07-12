package com.interviewsimulator.dto;

public record UserSummaryDto(String id, String email, String displayName, String role, String createdAt,
                              String firstName, String lastName, String birthDate, String gender,
                              String country, String employmentStatus, String educationStatus) {
}
