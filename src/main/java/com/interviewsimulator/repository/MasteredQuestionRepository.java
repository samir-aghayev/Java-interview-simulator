package com.interviewsimulator.repository;

import com.interviewsimulator.entity.MasteredQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MasteredQuestionRepository extends JpaRepository<MasteredQuestionEntity, UUID> {

    boolean existsByUser_IdAndQuestion_Id(UUID userId, UUID questionId);
}
