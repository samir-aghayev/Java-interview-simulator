package com.interviewsimulator.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "question_report")
public class QuestionReportEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionEntity question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity reporter;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolver_admin_id")
    private UserEntity resolver;

    protected QuestionReportEntity() {
    }

    public QuestionReportEntity(QuestionEntity question, UserEntity reporter, String message) {
        this.id = UUID.randomUUID();
        this.question = question;
        this.reporter = reporter;
        this.message = message;
        this.status = "OPEN";
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public QuestionEntity getQuestion() {
        return question;
    }

    public UserEntity getReporter() {
        return reporter;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public UserEntity getResolver() {
        return resolver;
    }

    public void resolve(String status, UserEntity resolver) {
        this.status = status;
        this.resolver = resolver;
        this.resolvedAt = LocalDateTime.now();
    }
}
