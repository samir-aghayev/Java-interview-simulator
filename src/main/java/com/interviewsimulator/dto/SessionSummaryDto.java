package com.interviewsimulator.dto;

public record SessionSummaryDto(String dateTime, int score, int correctAnswers, int totalQuestions,
                                 double percent) {
}
