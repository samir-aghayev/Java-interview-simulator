package com.interviewsimulator.dto;

import java.util.UUID;

public record AnswerSubmissionDto(UUID questionId, int selectedIndex, String perceivedDifficulty) {
}
