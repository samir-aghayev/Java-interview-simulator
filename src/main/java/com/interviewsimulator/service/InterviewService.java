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
import com.interviewsimulator.entity.UserEntity;
import com.interviewsimulator.repository.InterviewSessionRepository;
import com.interviewsimulator.repository.MasteredQuestionRepository;
import com.interviewsimulator.repository.QuestionOptionTranslationRepository;
import com.interviewsimulator.repository.QuestionRepository;
import com.interviewsimulator.repository.QuestionTranslationRepository;
import com.interviewsimulator.repository.UserRepository;
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
import java.util.UUID;

@Service
public class InterviewService {

    private static final double WEAK_TOPIC_THRESHOLD = 0.6;
    private static final String EASY_RATING = "EASY";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final QuestionRepository questionRepository;
    private final InterviewSessionRepository sessionRepository;
    private final MasteredQuestionRepository masteredQuestionRepository;
    private final UserRepository userRepository;
    private final QuestionTranslationRepository questionTranslationRepository;
    private final QuestionOptionTranslationRepository optionTranslationRepository;

    public InterviewService(QuestionRepository questionRepository,
                             InterviewSessionRepository sessionRepository,
                             MasteredQuestionRepository masteredQuestionRepository,
                             UserRepository userRepository,
                             QuestionTranslationRepository questionTranslationRepository,
                             QuestionOptionTranslationRepository optionTranslationRepository) {
        this.questionRepository = questionRepository;
        this.sessionRepository = sessionRepository;
        this.masteredQuestionRepository = masteredQuestionRepository;
        this.userRepository = userRepository;
        this.questionTranslationRepository = questionTranslationRepository;
        this.optionTranslationRepository = optionTranslationRepository;
    }

    /** Verilmiş suallar üçün lokala uyğun tərcümə mətnləri (tapılmayan üçün orijinala fallback). */
    private record Translations(Map<UUID, String> questionText, Map<UUID, String> optionText) {
        static final Translations EMPTY = new Translations(Map.of(), Map.of());
    }

    private Translations loadTranslations(String locale, List<QuestionEntity> questions) {
        if (locale == null || locale.isBlank() || "az".equalsIgnoreCase(locale.trim()) || questions.isEmpty()) {
            return Translations.EMPTY;
        }
        String normalized = locale.trim().toLowerCase();
        List<UUID> questionIds = questions.stream().map(QuestionEntity::getId).toList();
        List<UUID> optionIds = questions.stream()
                .flatMap(q -> q.getOptions().stream())
                .map(QuestionOptionEntity::getId)
                .toList();
        Map<UUID, String> questionText = new LinkedHashMap<>();
        questionTranslationRepository.findByLocaleAndQuestion_IdIn(normalized, questionIds)
                .forEach(tr -> questionText.put(tr.getQuestion().getId(), tr.getText()));
        Map<UUID, String> optionText = new LinkedHashMap<>();
        optionTranslationRepository.findByLocaleAndOption_IdIn(normalized, optionIds)
                .forEach(tr -> optionText.put(tr.getOption().getId(), tr.getText()));
        return new Translations(questionText, optionText);
    }

    @Transactional(readOnly = true)
    public List<QuestionDto> pickRandomQuestions(UUID userId, int count, String subject, List<String> topics,
                                                  String locale) {
        List<QuestionEntity> pool = new ArrayList<>(questionRepository.findAvailableForUser(userId));
        if (topics != null && !topics.isEmpty()) {
            pool.removeIf(q -> !topics.contains(q.getTopic()));
        } else if (subject != null && !subject.isBlank()) {
            pool.removeIf(q -> !subject.equals(q.getSubject()));
        }
        Collections.shuffle(pool);
        List<QuestionEntity> selected = pool.subList(0, Math.min(count, pool.size()));
        Translations translations = loadTranslations(locale, selected);
        List<QuestionDto> result = new ArrayList<>();
        for (QuestionEntity question : selected) {
            result.add(toDto(question, translations));
        }
        return result;
    }

    @Transactional
    public GradeResponse grade(UUID userId, List<AnswerSubmissionDto> submissions, String locale) {
        Map<String, Integer> topicCorrect = new LinkedHashMap<>();
        Map<String, Integer> topicTotal = new LinkedHashMap<>();
        List<QuestionResultDto> details = new ArrayList<>();
        int correctAnswers = 0;
        int score = 0;

        Map<UUID, QuestionEntity> questionsById = new LinkedHashMap<>();
        questionRepository.findAllById(submissions.stream().map(AnswerSubmissionDto::questionId).toList())
                .forEach(q -> questionsById.put(q.getId(), q));
        Translations translations = loadTranslations(locale, new ArrayList<>(questionsById.values()));

        for (AnswerSubmissionDto submission : submissions) {
            QuestionEntity question = questionsById.get(submission.questionId());
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
                        && !masteredQuestionRepository.existsByUser_IdAndQuestion_Id(userId, question.getId())) {
                    masteredQuestionRepository.save(new MasteredQuestionEntity(userRepository.getReferenceById(userId), question));
                }
            }
            details.add(new QuestionResultDto(question.getId().toString(), question.getTopic(),
                    translations.questionText().getOrDefault(question.getId(), question.getText()),
                    optionDtos(question, translations), selected, correctIndex, isCorrect));
        }

        List<String> weakTopics = new ArrayList<>(findWeakTopics(topicCorrect, topicTotal));
        int total = submissions.size();

        // Boş cavab siyahısı statistikaya sessiya kimi yazılmır — 0/0-lıq sessiyalar
        // orta göstəricini süni şəkildə aşağı salardı.
        if (total > 0) {
            UserEntity user = userRepository.getReferenceById(userId);
            InterviewSessionEntity session = new InterviewSessionEntity(user, total, correctAnswers, score);
            topicTotal.forEach((topic, totalCount) ->
                    session.addTopicStat(topic, topicCorrect.getOrDefault(topic, 0), totalCount));
            sessionRepository.save(session);
        }

        return new GradeResponse(total, correctAnswers, score, weakTopics, details);
    }

    @Transactional(readOnly = true)
    public WeakTopicsResponse weakTopicsSummary(UUID userId) {
        List<InterviewSessionEntity> candidateSessions = sessionRepository.findByUser_IdOrderByDateTimeDesc(userId);
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
    public ProgressResponse progressSummary(UUID userId) {
        List<InterviewSessionEntity> candidateSessions = sessionRepository.findByUser_IdOrderByDateTimeDesc(userId)
                .stream()
                .sorted(Comparator.comparing(InterviewSessionEntity::getDateTime))
                .toList();

        List<SessionSummaryDto> sessionList = new ArrayList<>();
        for (InterviewSessionEntity session : candidateSessions) {
            sessionList.add(new SessionSummaryDto(session.getDateTime().format(DATE_FORMAT), session.getScore(),
                    session.getCorrectAnswers(), session.getTotalQuestions(), sessionPercent(session)));
        }

        // Orta göstərici: sessiyaların düzgün cavab faizlərinin ortalaması. Xam balların
        // ortalaması sual sayından/çətinlikdən asılı olduğu üçün müqayisəyə yararsızdır.
        double averagePercent = candidateSessions.stream().mapToDouble(this::sessionPercent).average().orElse(0);
        Double improvementPercent = candidateSessions.size() > 1
                ? round1(sessionPercent(candidateSessions.get(candidateSessions.size() - 1))
                        - sessionPercent(candidateSessions.get(0)))
                : null;

        return new ProgressResponse(sessionList, round1(averagePercent), improvementPercent);
    }

    private double sessionPercent(InterviewSessionEntity session) {
        if (session.getTotalQuestions() == 0) {
            return 0;
        }
        return round1(100.0 * session.getCorrectAnswers() / session.getTotalQuestions());
    }

    private double round1(double value) {
        return Math.round(value * 10) / 10.0;
    }

    private QuestionDto toDto(QuestionEntity question, Translations translations) {
        return new QuestionDto(question.getId().toString(), question.getTopic(),
                translations.questionText().getOrDefault(question.getId(), question.getText()),
                optionDtos(question, translations));
    }

    private List<QuestionOptionDto> optionDtos(QuestionEntity question, Translations translations) {
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < question.getOptions().size(); i++) {
            order.add(i);
        }
        Collections.shuffle(order);

        List<QuestionOptionDto> shuffled = new ArrayList<>();
        for (int canonicalIndex : order) {
            QuestionOptionEntity option = question.getOptions().get(canonicalIndex);
            String text = translations.optionText().getOrDefault(option.getId(), option.getOptionText());
            shuffled.add(new QuestionOptionDto(canonicalIndex, text));
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
