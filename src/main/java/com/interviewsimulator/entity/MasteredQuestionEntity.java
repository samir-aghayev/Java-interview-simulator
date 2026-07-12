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
@Table(name = "mastered_question", uniqueConstraints = @UniqueConstraint(columnNames = {"candidate_name", "question_id"}))
public class MasteredQuestionEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "candidate_name", nullable = false)
    private String candidateName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionEntity question;

    protected MasteredQuestionEntity() {
    }

    public MasteredQuestionEntity(String candidateName, QuestionEntity question) {
        this.candidateName = candidateName;
        this.question = question;
    }

    public UUID getId() {
        return id;
    }

    public String getCandidateName() {
        return candidateName;
    }

    public QuestionEntity getQuestion() {
        return question;
    }
}
