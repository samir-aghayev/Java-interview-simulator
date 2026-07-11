package service;

import model.GradeResult;
import model.InterviewSession;
import model.Question;
import model.QuestionResult;
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
import java.util.UUID;
import java.util.stream.Collectors;

public class InterviewService {

    private static final double WEAK_TOPIC_THRESHOLD = 0.6;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final List<Question> questionBank = QuestionBank.getQuestions();
    private final List<InterviewSession> sessions = SessionRepository.getSessions();

    public List<Question> pickRandomQuestions(int count) {
        List<Question> pool = new ArrayList<>(questionBank);
        Collections.shuffle(pool);
        return new ArrayList<>(pool.subList(0, Math.min(count, pool.size())));
    }

    public GradeResult grade(String candidateName, Map<UUID, Integer> answersByQuestionId) {
        Map<String, Integer> topicCorrect = new LinkedHashMap<>();
        Map<String, Integer> topicTotal = new LinkedHashMap<>();
        List<QuestionResult> details = new ArrayList<>();
        int correctAnswers = 0;
        int score = 0;

        for (Map.Entry<UUID, Integer> entry : answersByQuestionId.entrySet()) {
            Question question = findQuestion(entry.getKey());
            if (question == null) {
                continue;
            }
            int selected = entry.getValue();
            boolean isCorrect = selected == question.getCorrectOptionIndex();
            topicTotal.merge(question.getTopic(), 1, Integer::sum);
            if (isCorrect) {
                correctAnswers++;
                score += question.getDifficulty().getPoints();
                topicCorrect.merge(question.getTopic(), 1, Integer::sum);
            }
            details.add(new QuestionResult(question, selected, isCorrect));
        }

        Set<String> weakTopics = findWeakTopics(topicCorrect, topicTotal);
        int total = answersByQuestionId.size();
        sessions.add(new InterviewSession(candidateName, total, correctAnswers, score, topicCorrect, topicTotal, weakTopics));
        return new GradeResult(total, correctAnswers, score, weakTopics, details);
    }

    public Map<String, Object> weakTopicsSummary(String candidateName) {
        List<InterviewSession> candidateSessions = sessionsFor(candidateName);
        Map<String, Object> response = new LinkedHashMap<>();
        if (candidateSessions.isEmpty()) {
            response.put("topics", List.of());
            response.put("weakTopics", List.of());
            return response;
        }

        Map<String, Integer> totalCorrect = new LinkedHashMap<>();
        Map<String, Integer> totalCount = new LinkedHashMap<>();
        for (InterviewSession session : candidateSessions) {
            session.getTopicTotalCounts().forEach((topic, count) -> totalCount.merge(topic, count, Integer::sum));
            session.getTopicCorrectCounts().forEach((topic, count) -> totalCorrect.merge(topic, count, Integer::sum));
        }

        List<Object> topics = new ArrayList<>();
        totalCount.forEach((topic, count) -> {
            int correct = totalCorrect.getOrDefault(topic, 0);
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("topic", topic);
            row.put("correct", correct);
            row.put("total", count);
            row.put("percent", Math.round(1000.0 * correct / count) / 10.0);
            topics.add(row);
        });

        response.put("topics", topics);
        response.put("weakTopics", new ArrayList<>(findWeakTopics(totalCorrect, totalCount)));
        return response;
    }

    public Map<String, Object> progressSummary(String candidateName) {
        List<InterviewSession> candidateSessions = sessionsFor(candidateName).stream()
                .sorted(Comparator.comparing(InterviewSession::getDateTime))
                .toList();

        Map<String, Object> response = new LinkedHashMap<>();
        List<Object> sessionList = new ArrayList<>();
        for (InterviewSession session : candidateSessions) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("dateTime", session.getDateTime().format(DATE_FORMAT));
            row.put("score", session.getScore());
            row.put("correctAnswers", session.getCorrectAnswers());
            row.put("totalQuestions", session.getTotalQuestions());
            sessionList.add(row);
        }
        response.put("sessions", sessionList);

        double averageScore = candidateSessions.stream().mapToInt(InterviewSession::getScore).average().orElse(0);
        response.put("averageScore", Math.round(averageScore * 10) / 10.0);

        if (candidateSessions.size() > 1) {
            int improvement = candidateSessions.get(candidateSessions.size() - 1).getScore()
                    - candidateSessions.get(0).getScore();
            response.put("improvement", improvement);
        }
        return response;
    }

    public void startInterview(Scanner scanner, String candidateName, int questionCount) {
        List<Question> selected = pickRandomQuestions(questionCount);
        Map<UUID, Integer> answers = new LinkedHashMap<>();

        for (int i = 0; i < selected.size(); i++) {
            Question question = selected.get(i);
            System.out.println("\nSual " + (i + 1) + "/" + selected.size() + " [" + question.getTopic() + "]");
            System.out.println(question.getText());
            List<String> options = question.getOptions();
            for (int j = 0; j < options.size(); j++) {
                System.out.println((j + 1) + ". " + options.get(j));
            }
            System.out.print("Cavabınız (1-" + options.size() + "): ");
            int answer = readOptionIndex(scanner, options.size());
            answers.put(question.getId(), answer - 1);
            System.out.println((answer - 1) == question.getCorrectOptionIndex()
                    ? "Düzgün!"
                    : "Səhv. Düzgün cavab: " + options.get(question.getCorrectOptionIndex()));
        }

        GradeResult result = grade(candidateName, answers);
        System.out.println("\n=== Nəticə ===");
        System.out.println("Düzgün cavablar: " + result.correctAnswers() + "/" + result.totalQuestions());
        System.out.println("Toplanan bal: " + result.score());
        System.out.println(result.weakTopics().isEmpty()
                ? "Zəif mövzu aşkarlanmadı. Əla nəticə!"
                : "Zəif mövzular: " + String.join(", ", result.weakTopics()));
    }

    public void showWeakTopics(String candidateName) {
        Map<String, Object> summary = weakTopicsSummary(candidateName);
        List<?> topics = (List<?>) summary.get("topics");
        if (topics.isEmpty()) {
            System.out.println("Bu istifadəçi üçün müsahibə tarixçəsi tapılmadı.");
            return;
        }
        System.out.println("=== Mövzu üzrə statistika ===");
        for (Object o : topics) {
            Map<?, ?> row = (Map<?, ?>) o;
            System.out.printf("%s: %d/%d (%.1f%%)%n", row.get("topic"), (Integer) row.get("correct"),
                    (Integer) row.get("total"), (Double) row.get("percent"));
        }
        List<?> weakTopics = (List<?>) summary.get("weakTopics");
        System.out.println(weakTopics.isEmpty()
                ? "Zəif mövzu yoxdur."
                : "Zəif mövzular: " + weakTopics.stream().map(String::valueOf).collect(Collectors.joining(", ")));
    }

    public void showProgressStatistics(String candidateName) {
        Map<String, Object> summary = progressSummary(candidateName);
        List<?> sessionList = (List<?>) summary.get("sessions");
        if (sessionList.isEmpty()) {
            System.out.println("Bu istifadəçi üçün müsahibə tarixçəsi tapılmadı.");
            return;
        }
        System.out.println("=== İnkişaf Statistikası ===");
        for (Object o : sessionList) {
            Map<?, ?> row = (Map<?, ?>) o;
            System.out.printf("%s | Bal: %d | Düzgün: %d/%d%n", row.get("dateTime"), (Integer) row.get("score"),
                    (Integer) row.get("correctAnswers"), (Integer) row.get("totalQuestions"));
        }
        System.out.printf("Orta bal: %.1f%n", (Double) summary.get("averageScore"));
        if (summary.containsKey("improvement")) {
            int improvement = (Integer) summary.get("improvement");
            System.out.println("İlk müsahibədən son müsahibəyə dəyişmə: " + (improvement >= 0 ? "+" : "") + improvement + " bal");
        }
    }

    private List<InterviewSession> sessionsFor(String candidateName) {
        return sessions.stream().filter(session -> session.getCandidateName().equals(candidateName)).toList();
    }

    private Question findQuestion(UUID id) {
        return questionBank.stream().filter(q -> q.getId().equals(id)).findFirst().orElse(null);
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
