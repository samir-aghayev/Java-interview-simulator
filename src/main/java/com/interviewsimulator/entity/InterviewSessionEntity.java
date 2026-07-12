package com.interviewsimulator.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "interview_session")
public class InterviewSessionEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "candidate_name", nullable = false)
    private String candidateName;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @Column(name = "correct_answers", nullable = false)
    private Integer correctAnswers;

    @Column(nullable = false)
    private Integer score;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SessionTopicStatEntity> topicStats = new ArrayList<>();

    protected InterviewSessionEntity() {
    }

    public InterviewSessionEntity(String candidateName, int totalQuestions, int correctAnswers, int score) {
        this.candidateName = candidateName;
        this.dateTime = LocalDateTime.now();
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.score = score;
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

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public Integer getCorrectAnswers() {
        return correctAnswers;
    }

    public Integer getScore() {
        return score;
    }

    public List<SessionTopicStatEntity> getTopicStats() {
        return topicStats;
    }

    public void addTopicStat(String topic, int correctCount, int totalCount) {
        topicStats.add(new SessionTopicStatEntity(this, topic, correctCount, totalCount));
    }
}
