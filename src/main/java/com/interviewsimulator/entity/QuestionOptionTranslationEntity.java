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
@Table(name = "question_option_translation", uniqueConstraints = @UniqueConstraint(columnNames = {"option_id", "locale"}))
public class QuestionOptionTranslationEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private QuestionOptionEntity option;

    @Column(nullable = false)
    private String locale;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    protected QuestionOptionTranslationEntity() {
    }

    public QuestionOptionTranslationEntity(QuestionOptionEntity option, String locale, String text) {
        this.id = UUID.randomUUID();
        this.option = option;
        this.locale = locale;
        this.text = text;
    }

    public UUID getId() {
        return id;
    }

    public QuestionOptionEntity getOption() {
        return option;
    }

    public String getLocale() {
        return locale;
    }

    public String getText() {
        return text;
    }
}
