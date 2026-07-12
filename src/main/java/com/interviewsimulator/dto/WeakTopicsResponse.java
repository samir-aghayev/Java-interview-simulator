package com.interviewsimulator.dto;

import java.util.List;

public record WeakTopicsResponse(List<TopicStatDto> topics, List<String> weakTopics) {
}
