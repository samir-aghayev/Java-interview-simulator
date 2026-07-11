package service;

import model.AnswerSubmission;
import model.GradeResult;
import model.InterviewSession;
import model.Question;
import model.QuestionResult;
import repository.MasteryRepository;
import repository.QuestionBank;
import repository.SessionRepository;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InterviewService {

    private static final double WEAK_TOPIC_THRESHOLD = 0.6;
    private static final String EASY_RATING = "EASY";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final List<Question> questionBank = QuestionBank.getQuestions();
    private final List<InterviewSession> sessions = SessionRepository.getSessions();
    private final Map<String, Set<UUID>> masteredQuestions = MasteryRepository.getMasteredQuestions();

    public List<Question> pickRandomQuestions(String candidateName, int count) {
        Set<UUID> mastered = masteredQuestions.getOrDefault(candidateName, Set.of());
        List<Question> pool = questionBank.stream()
                .filter(q -> !mastered.contains(q.getId()))
                .collect(Collectors.toCollection(ArrayList::new));
        Collections.shuffle(pool);
        return new ArrayList<>(pool.subList(0, Math.min(count, pool.size())));
    }

    public GradeResult grade(String candidateName, List<AnswerSubmission> submissions) {
        Map<String, Integer> topicCorrect = new LinkedHashMap<>();
        Map<String, Integer> topicTotal = new LinkedHashMap<>();
        List<QuestionResult> details = new ArrayList<>();
        int correctAnswers = 0;
        int score = 0;

        for (AnswerSubmission submission : submissions) {
            Question question = findQuestion(submission.questionId());
            if (question == null) {
                continue;
            }
            int selected = submission.selectedIndex();
            boolean isCorrect = selected == question.getCorrectOptionIndex();
            topicTotal.merge(question.getTopic(), 1, Integer::sum);
            if (isCorrect) {
                correctAnswers++;
                score += question.getDifficulty().getPoints();
                topicCorrect.merge(question.getTopic(), 1, Integer::sum);
                if (EASY_RATING.equalsIgnoreCase(submission.perceivedDifficulty())) {
                    masteredQuestions.computeIfAbsent(candidateName, k -> ConcurrentHashMap.newKeySet()).add(question.getId());
                }
            }
            details.add(new QuestionResult(question, selected, isCorrect));
        }

        Set<String> weakTopics = findWeakTopics(topicCorrect, topicTotal);
        int total = submissions.size();
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

    private List<InterviewSession> sessionsFor(String candidateName) {
        return sessions.stream().filter(session -> session.getCandidateName().equals(candidateName)).toList();
    }

    private Question findQuestion(UUID id) {
        return questionBank.stream().filter(q -> q.getId().equals(id)).findFirst().orElse(null);
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
