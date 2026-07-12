package com.interviewsimulator.dto;

import java.util.List;

public record QuizStartRequest(Integer questionCount, String subject, List<String> topics) {
}
