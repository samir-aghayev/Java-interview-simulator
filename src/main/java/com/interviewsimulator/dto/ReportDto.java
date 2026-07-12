package com.interviewsimulator.dto;

public record ReportDto(String id, String questionId, String questionText, String reporterEmail,
                         String message, String status, String createdAt) {
}
