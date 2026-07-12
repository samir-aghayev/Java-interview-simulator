package com.interviewsimulator.dto;

import java.util.List;

public record QuizSubmitRequest(List<AnswerSubmissionDto> answers, String locale) {
}
