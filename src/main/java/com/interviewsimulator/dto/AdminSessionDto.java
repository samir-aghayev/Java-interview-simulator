package com.interviewsimulator.dto;

public record AdminSessionDto(String id, String userEmail, String userDisplayName, int totalQuestions,
                               int correctAnswers, int score, int percent, String dateTime) {
}
