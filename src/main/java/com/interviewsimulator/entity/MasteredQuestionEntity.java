package com.interviewsimulator.entity;

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
@Table(name = "mastered_question", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "question_id"}))
public class MasteredQuestionEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionEntity question;

    protected MasteredQuestionEntity() {
    }

    public MasteredQuestionEntity(UserEntity user, QuestionEntity question) {
        this.user = user;
        this.question = question;
    }

    public UUID getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public QuestionEntity getQuestion() {
        return question;
    }
}
