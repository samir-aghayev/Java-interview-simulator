package com.interviewsimulator.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

@Entity
@Table(name = "session_topic_stat", uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "topic"}))
public class SessionTopicStatEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private InterviewSessionEntity session;

    @Column(nullable = false)
    private String topic;

    @Column(name = "correct_count", nullable = false)
    private Integer correctCount;

    @Column(name = "total_count", nullable = false)
    private Integer totalCount;

    protected SessionTopicStatEntity() {
    }

    public SessionTopicStatEntity(InterviewSessionEntity session, String topic, int correctCount, int totalCount) {
        this.session = session;
        this.topic = topic;
        this.correctCount = correctCount;
        this.totalCount = totalCount;
    }

    public UUID getId() {
        return id;
    }

    public InterviewSessionEntity getSession() {
        return session;
    }

    public String getTopic() {
        return topic;
    }

    public Integer getCorrectCount() {
        return correctCount;
    }

    public Integer getTotalCount() {
        return totalCount;
    }
}
