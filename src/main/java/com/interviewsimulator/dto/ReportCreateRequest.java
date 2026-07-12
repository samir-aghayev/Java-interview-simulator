package com.interviewsimulator.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ReportCreateRequest(
        @NotNull UUID questionId,
        @NotNull @Size(min = 5, max = 2000) String message) {
}
