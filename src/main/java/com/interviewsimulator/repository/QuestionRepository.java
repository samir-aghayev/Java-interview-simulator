package com.interviewsimulator.repository;

import com.interviewsimulator.entity.QuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<QuestionEntity, UUID> {

    @org.springframework.data.jpa.repository.Query(
            "select q from QuestionEntity q where q.id not in "
                    + "(select m.question.id from MasteredQuestionEntity m where m.candidateName = :candidateName)")
    List<QuestionEntity> findAvailableForCandidate(String candidateName);
}
