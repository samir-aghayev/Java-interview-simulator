package service;

import model.InterviewSession;
import model.Question;
import repository.QuestionBank;
import repository.SessionRepository;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;

public class InterviewService {

    private static final double WEAK_TOPIC_THRESHOLD = 0.6;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final List<Question> questionBank = QuestionBank.getQuestions();
    private final List<InterviewSession> sessions = SessionRepository.getSessions();

    public void startInterview(Scanner scanner, String candidateName, int questionCount) {
        List<Question> pool = new ArrayList<>(questionBank);
        Collections.shuffle(pool);
        int count = Math.min(questionCount, pool.size());
        List<Question> selected = pool.subList(0, count);

        Map<String, Integer> topicCorrect = new LinkedHashMap<>();
        Map<String, Integer> topicTotal = new LinkedHashMap<>();
        int correctAnswers = 0;
        int score = 0;

        for (int i = 0; i < selected.size(); i++) {
            Question question = selected.get(i);
            System.out.println("\nSual " + (i + 1) + "/" + count + " [" + question.getTopic() + "]");
            System.out.println(question.getText());
            List<String> options = question.getOptions();
            for (int j = 0; j < options.size(); j++) {
                System.out.println((j + 1) + ". " + options.get(j));
            }
            System.out.print("Cavabınız (1-" + options.size() + "): ");
            int answer = readOptionIndex(scanner, options.size());

            topicTotal.merge(question.getTopic(), 1, Integer::sum);
            if ((answer - 1) == question.getCorrectOptionIndex()) {
                correctAnswers++;
                score += question.getDifficulty().getPoints();
                topicCorrect.merge(question.getTopic(), 1, Integer::sum);
                System.out.println("Düzgün!");
            } else {
                System.out.println("Səhv. Düzgün cavab: " + options.get(question.getCorrectOptionIndex()));
            }
        }

        Set<String> weakTopics = findWeakTopics(topicCorrect, topicTotal);
        sessions.add(new InterviewSession(candidateName, count, correctAnswers, score, topicCorrect, topicTotal, weakTopics));

        System.out.println("\n=== Nəticə ===");
        System.out.println("Düzgün cavablar: " + correctAnswers + "/" + count);
        System.out.println("Toplanan bal: " + score);
        System.out.println(weakTopics.isEmpty()
                ? "Zəif mövzu aşkarlanmadı. Əla nəticə!"
                : "Zəif mövzular: " + String.join(", ", weakTopics));
    }

    public void showWeakTopics(String candidateName) {
        List<InterviewSession> candidateSessions = sessions.stream()
                .filter(session -> session.getCandidateName().equals(candidateName))
                .toList();
        if (candidateSessions.isEmpty()) {
            System.out.println("Bu istifadəçi üçün müsahibə tarixçəsi tapılmadı.");
            return;
        }

        Map<String, Integer> totalCorrect = new LinkedHashMap<>();
        Map<String, Integer> totalCount = new LinkedHashMap<>();
        for (InterviewSession session : candidateSessions) {
            session.getTopicTotalCounts().forEach((topic, count) -> totalCount.merge(topic, count, Integer::sum));
            session.getTopicCorrectCounts().forEach((topic, count) -> totalCorrect.merge(topic, count, Integer::sum));
        }

        System.out.println("=== Mövzu üzrə statistika ===");
        totalCount.forEach((topic, count) -> {
            int correct = totalCorrect.getOrDefault(topic, 0);
            System.out.printf("%s: %d/%d (%.1f%%)%n", topic, correct, count, 100.0 * correct / count);
        });

        Set<String> weakTopics = findWeakTopics(totalCorrect, totalCount);
        System.out.println(weakTopics.isEmpty()
                ? "Zəif mövzu yoxdur."
                : "Zəif mövzular: " + String.join(", ", weakTopics));
    }

    public void showProgressStatistics(String candidateName) {
        List<InterviewSession> candidateSessions = sessions.stream()
                .filter(session -> session.getCandidateName().equals(candidateName))
                .sorted(Comparator.comparing(InterviewSession::getDateTime))
                .toList();
        if (candidateSessions.isEmpty()) {
            System.out.println("Bu istifadəçi üçün müsahibə tarixçəsi tapılmadı.");
            return;
        }

        System.out.println("=== İnkişaf Statistikası ===");
        for (InterviewSession session : candidateSessions) {
            System.out.printf("%s | Bal: %d | Düzgün: %d/%d%n",
                    session.getDateTime().format(DATE_FORMAT), session.getScore(),
                    session.getCorrectAnswers(), session.getTotalQuestions());
        }

        double averageScore = candidateSessions.stream().mapToInt(InterviewSession::getScore).average().orElse(0);
        System.out.printf("Orta bal: %.1f%n", averageScore);

        if (candidateSessions.size() > 1) {
            int improvement = candidateSessions.get(candidateSessions.size() - 1).getScore() - candidateSessions.get(0).getScore();
            System.out.println("İlk müsahibədən son müsahibəyə dəyişmə: " + (improvement >= 0 ? "+" : "") + improvement + " bal");
        }
    }

    private int readOptionIndex(Scanner scanner, int optionCount) {
        while (true) {
            String input = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(input);
                if (value >= 1 && value <= optionCount) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.print("Zəhmət olmasa 1-" + optionCount + " arasında rəqəm daxil edin: ");
        }
    }

    private Set<String> findWeakTopics(Map<String, Integer> correct, Map<String, Integer> total) {
        Set<String> weakTopics = new TreeSet<>();
        for (Map.Entry<String, Integer> entry : total.entrySet()) {
            int correctCount = correct.getOrDefault(entry.getKey(), 0);
            if ((double) correctCount / entry.getValue() < WEAK_TOPIC_THRESHOLD) {
                weakTopics.add(entry.getKey());
            }
        }
        return weakTopics;
    }
}
