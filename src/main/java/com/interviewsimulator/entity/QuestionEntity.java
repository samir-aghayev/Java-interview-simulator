package com.interviewsimulator.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "question")
public class QuestionEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(nullable = false)
    private String difficulty;

    @Column(nullable = false)
    private Boolean active = true;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<QuestionOptionEntity> options = new ArrayList<>();

    protected QuestionEntity() {
    }

    public QuestionEntity(String topic, String text, String difficulty) {
        this.id = UUID.randomUUID();
        this.topic = topic;
        this.text = text;
        this.difficulty = difficulty;
        this.active = true;
    }

    public UUID getId() {
        return id;
    }

    public String getTopic() {
        return topic;
    }

    public String getText() {
        return text;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public Boolean getActive() {
        return active;
    }

    public List<QuestionOptionEntity> getOptions() {
        return options;
    }

    public void updateContent(String topic, String text, String difficulty) {
        this.topic = topic;
        this.text = text;
        this.difficulty = difficulty;
    }

    public void replaceOptions(List<String> optionTexts, int correctIndex) {
        options.clear();
        for (int i = 0; i < optionTexts.size(); i++) {
            options.add(new QuestionOptionEntity(this, i, optionTexts.get(i), i == correctIndex));
        }
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
