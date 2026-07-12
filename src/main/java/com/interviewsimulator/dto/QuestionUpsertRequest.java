package com.interviewsimulator.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record QuestionUpsertRequest(
        @NotBlank String subject,
        @NotBlank String topic,
        @NotBlank String text,
        @NotBlank String difficulty,
        @NotNull @Size(min = 2, max = 6) List<@NotBlank String> options,
        @NotNull Integer correctIndex) {
}
