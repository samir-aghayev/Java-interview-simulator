package com.interviewsimulator.dto;

import java.util.List;

public record QuestionResultDto(String id, String topic, String text, List<QuestionOptionDto> options,
                                 int selectedIndex, int correctIndex, boolean correct) {
}
