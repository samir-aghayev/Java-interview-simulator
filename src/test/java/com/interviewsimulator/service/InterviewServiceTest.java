package com.interviewsimulator.service;

import com.interviewsimulator.dto.AnswerSubmissionDto;
import com.interviewsimulator.dto.GradeResponse;
import com.interviewsimulator.dto.WeakTopicsResponse;
import com.interviewsimulator.entity.InterviewSessionEntity;
import com.interviewsimulator.entity.QuestionEntity;
import com.interviewsimulator.entity.SessionTopicStatEntity;
import com.interviewsimulator.entity.UserEntity;
import com.interviewsimulator.repository.InterviewSessionRepository;
import com.interviewsimulator.repository.MasteredQuestionRepository;
import com.interviewsimulator.repository.QuestionOptionTranslationRepository;
import com.interviewsimulator.repository.QuestionRepository;
import com.interviewsimulator.repository.QuestionTranslationRepository;
import com.interviewsimulator.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InterviewServiceTest {

    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private InterviewSessionRepository sessionRepository;
    @Mock
    private MasteredQuestionRepository masteredQuestionRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private QuestionTranslationRepository questionTranslationRepository;
    @Mock
    private QuestionOptionTranslationRepository optionTranslationRepository;

    private InterviewService interviewService;
    private UUID userId;

    @BeforeEach
    void setUp() {
        interviewService = new InterviewService(questionRepository, sessionRepository, masteredQuestionRepository,
                userRepository, questionTranslationRepository, optionTranslationRepository);
        userId = UUID.randomUUID();
    }

    private QuestionEntity question(String topic, String difficulty, int correctIndex) {
        QuestionEntity question = new QuestionEntity("Java", topic, "Sual mətni?", difficulty);
        question.replaceOptions(List.of("A", "B", "C", "D"), correctIndex);
        return question;
    }

    @Test
    void gradeAwardsPointsAndMarksMasteredOnCorrectEasyAnswer() {
        QuestionEntity question = question("OOP", "EASY", 1);
        when(questionRepository.findAllById(anyList())).thenReturn(List.of(question));
        when(userRepository.getReferenceById(userId)).thenReturn(new UserEntity("a@b.com", "hash", "A B", "USER"));
        when(masteredQuestionRepository.existsByUser_IdAndQuestion_Id(userId, question.getId())).thenReturn(false);

        AnswerSubmissionDto submission = new AnswerSubmissionDto(question.getId(), 1, "EASY");
        GradeResponse response = interviewService.grade(userId, List.of(submission), null);

        assertThat(response.correctAnswers()).isEqualTo(1);
        assertThat(response.score()).isEqualTo(5);
        assertThat(response.weakTopics()).isEmpty();
        assertThat(response.details().get(0).correct()).isTrue();
        verify(masteredQuestionRepository).save(any());
        verify(sessionRepository).save(any(InterviewSessionEntity.class));
    }

    @Test
    void gradeGivesNoPointsForWrongAnswerAndFlagsWeakTopic() {
        QuestionEntity question = question("Kolleksiyalar", "MEDIUM", 0);
        when(questionRepository.findAllById(anyList())).thenReturn(List.of(question));
        when(userRepository.getReferenceById(userId)).thenReturn(new UserEntity("a@b.com", "hash", "A B", "USER"));

        AnswerSubmissionDto submission = new AnswerSubmissionDto(question.getId(), 2, "MEDIUM");
        GradeResponse response = interviewService.grade(userId, List.of(submission), null);

        assertThat(response.correctAnswers()).isZero();
        assertThat(response.score()).isZero();
        assertThat(response.weakTopics()).containsExactly("Kolleksiyalar");
        verify(masteredQuestionRepository, never()).save(any());
    }

    @Test
    void gradeDoesNotPersistSessionForEmptySubmissions() {
        GradeResponse response = interviewService.grade(userId, List.of(), null);

        assertThat(response.totalQuestions()).isZero();
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void weakTopicsSummaryAggregatesAcrossSessions() {
        InterviewSessionEntity session1 = new InterviewSessionEntity(
                new UserEntity("a@b.com", "hash", "A B", "USER"), 4, 1, 5);
        session1.addTopicStat("Multithreading", 1, 4);
        InterviewSessionEntity session2 = new InterviewSessionEntity(
                new UserEntity("a@b.com", "hash", "A B", "USER"), 4, 4, 20);
        session2.addTopicStat("OOP", 4, 4);

        when(sessionRepository.findByUser_IdOrderByDateTimeDesc(userId)).thenReturn(List.of(session1, session2));

        WeakTopicsResponse response = interviewService.weakTopicsSummary(userId);

        assertThat(response.weakTopics()).containsExactly("Multithreading");
        assertThat(response.topics()).hasSize(2);
    }

    @Test
    void weakTopicsSummaryReturnsEmptyWhenNoHistory() {
        when(sessionRepository.findByUser_IdOrderByDateTimeDesc(userId)).thenReturn(List.of());

        WeakTopicsResponse response = interviewService.weakTopicsSummary(userId);

        assertThat(response.topics()).isEmpty();
        assertThat(response.weakTopics()).isEmpty();
    }

    @Test
    void pickRandomQuestionsFiltersByTopicsAndCapsAtCount() {
        QuestionEntity oop = question("OOP", "EASY", 0);
        QuestionEntity mt = question("Multithreading", "EASY", 0);
        when(questionRepository.findAvailableForUser(userId)).thenReturn(List.of(oop, mt));

        List<?> result = interviewService.pickRandomQuestions(userId, 5, null, List.of("OOP"), null);

        assertThat(result).hasSize(1);
    }
}
