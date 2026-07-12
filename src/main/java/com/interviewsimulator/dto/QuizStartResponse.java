package com.interviewsimulator.dto;

import java.util.List;

public record QuizStartResponse(List<QuestionDto> questions) {
}
