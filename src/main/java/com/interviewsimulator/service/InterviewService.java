package com.interviewsimulator.service;

import com.interviewsimulator.dto.AnswerSubmissionDto;
import com.interviewsimulator.dto.GradeResponse;
import com.interviewsimulator.dto.ProgressResponse;
import com.interviewsimulator.dto.QuestionDto;
import com.interviewsimulator.dto.QuestionOptionDto;
import com.interviewsimulator.dto.QuestionResultDto;
import com.interviewsimulator.dto.SessionSummaryDto;
import com.interviewsimulator.dto.TopicStatDto;
import com.interviewsimulator.dto.WeakTopicsResponse;
import com.interviewsimulator.entity.InterviewSessionEntity;
import com.interviewsimulator.entity.MasteredQuestionEntity;
import com.interviewsimulator.entity.QuestionEntity;
import com.interviewsimulator.entity.QuestionOptionEntity;
import com.interviewsimulator.repository.InterviewSessionRepository;
import com.interviewsimulator.repository.MasteredQuestionRepository;
import com.interviewsimulator.repository.QuestionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

@Service
public class InterviewService {

    private static final double WEAK_TOPIC_THRESHOLD = 0.6;
    private static final String EASY_RATING = "EASY";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final QuestionRepository questionRepository;
    private final InterviewSessionRepository sessionRepository;
    private final MasteredQuestionRepository masteredQuestionRepository;

    public InterviewService(QuestionRepository questionRepository,
                             InterviewSessionRepository sessionRepository,
                             MasteredQuestionRepository masteredQuestionRepository) {
        this.questionRepository = questionRepository;
        this.sessionRepository = sessionRepository;
        this.masteredQuestionRepository = masteredQuestionRepository;
    }

    @Transactional(readOnly = true)
    public List<QuestionDto> pickRandomQuestions(String candidateName, int count) {
        List<QuestionEntity> pool = new ArrayList<>(questionRepository.findAvailableForCandidate(candidateName));
        Collections.shuffle(pool);
        List<QuestionEntity> selected = pool.subList(0, Math.min(count, pool.size()));
        List<QuestionDto> result = new ArrayList<>();
        for (QuestionEntity question : selected) {
            result.add(toDto(question));
        }
        return result;
    }

    @Transactional
    public GradeResponse grade(String candidateName, List<AnswerSubmissionDto> submissions) {
        Map<String, Integer> topicCorrect = new LinkedHashMap<>();
        Map<String, Integer> topicTotal = new LinkedHashMap<>();
        List<QuestionResultDto> details = new ArrayList<>();
        int correctAnswers = 0;
        int score = 0;

        for (AnswerSubmissionDto submission : submissions) {
            QuestionEntity question = questionRepository.findById(submission.questionId()).orElse(null);
            if (question == null) {
                continue;
            }
            int selected = submission.selectedIndex();
            int correctIndex = correctIndex(question);
            boolean isCorrect = selected == correctIndex;
            topicTotal.merge(question.getTopic(), 1, Integer::sum);
            if (isCorrect) {
                correctAnswers++;
                score += pointsFor(question.getDifficulty());
                topicCorrect.merge(question.getTopic(), 1, Integer::sum);
                if (EASY_RATING.equalsIgnoreCase(submission.perceivedDifficulty())
                        && !masteredQuestionRepository.existsByCandidateNameAndQuestionId(candidateName, question.getId())) {
                    masteredQuestionRepository.save(new MasteredQuestionEntity(candidateName, question));
                }
            }
            details.add(new QuestionResultDto(question.getId().toString(), question.getTopic(), question.getText(),
                    optionDtos(question), selected, correctIndex, isCorrect));
        }

        List<String> weakTopics = new ArrayList<>(findWeakTopics(topicCorrect, topicTotal));
        int total = submissions.size();

        InterviewSessionEntity session = new InterviewSessionEntity(candidateName, total, correctAnswers, score);
        topicTotal.forEach((topic, totalCount) ->
                session.addTopicStat(topic, topicCorrect.getOrDefault(topic, 0), totalCount));
        sessionRepository.save(session);

        return new GradeResponse(total, correctAnswers, score, weakTopics, details);
    }

    @Transactional(readOnly = true)
    public WeakTopicsResponse weakTopicsSummary(String candidateName) {
        List<InterviewSessionEntity> candidateSessions = sessionRepository.findByCandidateNameOrderByDateTimeDesc(candidateName);
        if (candidateSessions.isEmpty()) {
            return new WeakTopicsResponse(List.of(), List.of());
        }

        Map<String, Integer> totalCorrect = new LinkedHashMap<>();
        Map<String, Integer> totalCount = new LinkedHashMap<>();
        candidateSessions.forEach(session -> session.getTopicStats().forEach(stat -> {
            totalCount.merge(stat.getTopic(), stat.getTotalCount(), Integer::sum);
            totalCorrect.merge(stat.getTopic(), stat.getCorrectCount(), Integer::sum);
        }));

        List<TopicStatDto> topics = new ArrayList<>();
        totalCount.forEach((topic, count) -> {
            int correct = totalCorrect.getOrDefault(topic, 0);
            double percent = Math.round(1000.0 * correct / count) / 10.0;
            topics.add(new TopicStatDto(topic, correct, count, percent));
        });

        return new WeakTopicsResponse(topics, new ArrayList<>(findWeakTopics(totalCorrect, totalCount)));
    }

    @Transactional(readOnly = true)
    public ProgressResponse progressSummary(String candidateName) {
        List<InterviewSessionEntity> candidateSessions = sessionRepository.findByCandidateNameOrderByDateTimeDesc(candidateName)
                .stream()
                .sorted(Comparator.comparing(InterviewSessionEntity::getDateTime))
                .toList();

        List<SessionSummaryDto> sessionList = new ArrayList<>();
        for (InterviewSessionEntity session : candidateSessions) {
            sessionList.add(new SessionSummaryDto(session.getDateTime().format(DATE_FORMAT), session.getScore(),
                    session.getCorrectAnswers(), session.getTotalQuestions()));
        }

        double averageScore = candidateSessions.stream().mapToInt(InterviewSessionEntity::getScore).average().orElse(0);
        Integer improvement = candidateSessions.size() > 1
                ? candidateSessions.get(candidateSessions.size() - 1).getScore() - candidateSessions.get(0).getScore()
                : null;

        return new ProgressResponse(sessionList, Math.round(averageScore * 10) / 10.0, improvement);
    }

    private QuestionDto toDto(QuestionEntity question) {
        return new QuestionDto(question.getId().toString(), question.getTopic(), question.getText(), optionDtos(question));
    }

    private List<QuestionOptionDto> optionDtos(QuestionEntity question) {
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < question.getOptions().size(); i++) {
            order.add(i);
        }
        Collections.shuffle(order);

        List<QuestionOptionDto> shuffled = new ArrayList<>();
        for (int canonicalIndex : order) {
            QuestionOptionEntity option = question.getOptions().get(canonicalIndex);
            shuffled.add(new QuestionOptionDto(canonicalIndex, option.getOptionText()));
        }
        return shuffled;
    }

    private int correctIndex(QuestionEntity question) {
        List<QuestionOptionEntity> options = question.getOptions();
        for (int i = 0; i < options.size(); i++) {
            if (Boolean.TRUE.equals(options.get(i).getCorrect())) {
                return i;
            }
        }
        throw new IllegalStateException("Question has no correct option: " + question.getId());
    }

    private int pointsFor(String difficulty) {
        return switch (difficulty.toUpperCase()) {
            case "EASY" -> 5;
            case "MEDIUM" -> 10;
            case "HARD" -> 15;
            default -> throw new IllegalStateException("Unknown difficulty: " + difficulty);
        };
    }

    private List<String> findWeakTopics(Map<String, Integer> correct, Map<String, Integer> total) {
        TreeSet<String> weakTopics = new TreeSet<>();
        for (Map.Entry<String, Integer> entry : total.entrySet()) {
            int correctCount = correct.getOrDefault(entry.getKey(), 0);
            if ((double) correctCount / entry.getValue() < WEAK_TOPIC_THRESHOLD) {
                weakTopics.add(entry.getKey());
            }
        }
        return new ArrayList<>(weakTopics);
    }
}
