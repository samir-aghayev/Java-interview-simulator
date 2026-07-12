package com.interviewsimulator.dto;

import java.util.List;

public record QuizSubmitRequest(String candidateName, List<AnswerSubmissionDto> answers) {
}
