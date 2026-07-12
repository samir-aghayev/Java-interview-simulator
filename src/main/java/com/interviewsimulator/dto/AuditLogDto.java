package com.interviewsimulator.dto;

public record AuditLogDto(String id, String adminEmail, String action, String targetType, String targetId,
                           String details, String createdAt) {
}
