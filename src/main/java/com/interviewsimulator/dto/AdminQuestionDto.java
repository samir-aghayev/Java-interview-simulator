package com.interviewsimulator.dto;

import java.util.List;

public record AdminQuestionDto(String id, String topic, String text, String difficulty, boolean active,
                                List<AdminQuestionOptionDto> options) {
}
