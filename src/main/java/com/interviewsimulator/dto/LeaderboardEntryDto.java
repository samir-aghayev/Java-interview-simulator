package com.interviewsimulator.dto;

public record LeaderboardEntryDto(int rank, String displayName, int sessionsCount, int totalScore,
                                   double averagePercent, boolean isCurrentUser) {
}
