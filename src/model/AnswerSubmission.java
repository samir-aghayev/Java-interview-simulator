package model;

import java.util.UUID;

public record AnswerSubmission(UUID questionId, int selectedIndex, String perceivedDifficulty) {
}
