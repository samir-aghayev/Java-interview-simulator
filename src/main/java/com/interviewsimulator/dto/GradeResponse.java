package com.interviewsimulator.dto;

import java.util.List;

public record GradeResponse(int totalQuestions, int correctAnswers, int score, List<String> weakTopics,
                             List<QuestionResultDto> details) {
}
