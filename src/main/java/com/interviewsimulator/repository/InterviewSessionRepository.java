package com.interviewsimulator.repository;

import com.interviewsimulator.entity.InterviewSessionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface InterviewSessionRepository extends JpaRepository<InterviewSessionEntity, UUID> {

    List<InterviewSessionEntity> findByUser_IdOrderByDateTimeDesc(UUID userId);

    Page<InterviewSessionEntity> findAllByOrderByDateTimeDesc(Pageable pageable);

    Page<InterviewSessionEntity> findByUser_EmailContainingIgnoreCaseOrUser_DisplayNameContainingIgnoreCaseOrderByDateTimeDesc(
            String email, String displayName, Pageable pageable);

    interface LeaderboardRow {
        UUID getUserId();
        String getDisplayName();
        Long getSessionsCount();
        Long getTotalScore();
        Long getCorrectSum();
        Long getTotalSum();
    }

    @Query("select s.user.id as userId, s.user.displayName as displayName, count(s) as sessionsCount, "
            + "sum(s.score) as totalScore, sum(s.correctAnswers) as correctSum, sum(s.totalQuestions) as totalSum "
            + "from InterviewSessionEntity s group by s.user.id, s.user.displayName "
            + "having count(s) >= :minSessions")
    List<LeaderboardRow> aggregateLeaderboard(@Param("minSessions") long minSessions);
}
