package com.interviewsimulator.dto;

import java.util.List;

public record LeaderboardResponse(List<LeaderboardEntryDto> entries, int minSessionsRequired, int minQuestionsRequired) {
}
