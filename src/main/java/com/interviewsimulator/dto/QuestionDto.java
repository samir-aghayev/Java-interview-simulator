package com.interviewsimulator.dto;

import java.util.List;

public record QuestionDto(String id, String topic, String text, List<QuestionOptionDto> options) {
}
