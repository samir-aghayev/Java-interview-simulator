package com.interviewsimulator.repository;

import com.interviewsimulator.entity.MasteredQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MasteredQuestionRepository extends JpaRepository<MasteredQuestionEntity, UUID> {

    List<MasteredQuestionEntity> findByCandidateName(String candidateName);

    boolean existsByCandidateNameAndQuestionId(String candidateName, UUID questionId);
}
