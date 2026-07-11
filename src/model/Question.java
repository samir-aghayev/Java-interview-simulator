package model;

import java.util.List;
import java.util.UUID;

public class Question {
    private final UUID id;
    private final String topic;
    private final String text;
    private final List<String> options;
    private final int correctOptionIndex;
    private final Difficulty difficulty;

    public Question(String topic, String text, List<String> options, int correctOptionIndex, Difficulty difficulty) {
        this.id = UUID.randomUUID();
        this.topic = topic;
        this.text = text;
        this.options = options;
        this.correctOptionIndex = correctOptionIndex;
        this.difficulty = difficulty;
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

    public List<String> getOptions() {
        return options;
    }

    public int getCorrectOptionIndex() {
        return correctOptionIndex;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }
}
