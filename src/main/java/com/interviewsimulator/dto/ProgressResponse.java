package com.interviewsimulator.dto;

import java.util.List;

public record ProgressResponse(List<SessionSummaryDto> sessions, double averageScore, Integer improvement) {
}
