package com.interviewsimulator.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

@Entity
@Table(name = "question_translation", uniqueConstraints = @UniqueConstraint(columnNames = {"question_id", "locale"}))
public class QuestionTranslationEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuestionEntity question;

    @Column(nullable = false)
    private String locale;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    protected QuestionTranslationEntity() {
    }

    public QuestionTranslationEntity(QuestionEntity question, String locale, String text) {
        this.id = UUID.randomUUID();
        this.question = question;
        this.locale = locale;
        this.text = text;
    }

    public UUID getId() {
        return id;
    }

    public QuestionEntity getQuestion() {
        return question;
    }

    public String getLocale() {
        return locale;
    }

    public String getText() {
        return text;
    }
}
