package model;

import java.util.List;
import java.util.Set;

public record GradeResult(int totalQuestions, int correctAnswers, int score, Set<String> weakTopics,
                           List<QuestionResult> details) {
}
