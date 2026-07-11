package model;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class InterviewSession {
    private final UUID id;
    private final String candidateName;
    private final LocalDateTime dateTime;
    private final int totalQuestions;
    private final int correctAnswers;
    private final int score;
    private final Map<String, Integer> topicCorrectCounts;
    private final Map<String, Integer> topicTotalCounts;
    private final Set<String> weakTopics;

    public InterviewSession(String candidateName, int totalQuestions, int correctAnswers, int score,
                             Map<String, Integer> topicCorrectCounts, Map<String, Integer> topicTotalCounts,
                             Set<String> weakTopics) {
        this.id = UUID.randomUUID();
        this.candidateName = candidateName;
        this.dateTime = LocalDateTime.now();
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.score = score;
        this.topicCorrectCounts = topicCorrectCounts;
        this.topicTotalCounts = topicTotalCounts;
        this.weakTopics = weakTopics;
    }

    public UUID getId() {
        return id;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public int getScore() {
        return score;
    }

    public Map<String, Integer> getTopicCorrectCounts() {
        return topicCorrectCounts;
    }

    public Map<String, Integer> getTopicTotalCounts() {
        return topicTotalCounts;
    }

    public Set<String> getWeakTopics() {
        return weakTopics;
    }
}
