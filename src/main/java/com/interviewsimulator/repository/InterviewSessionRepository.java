package com.interviewsimulator.repository;

import com.interviewsimulator.entity.InterviewSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InterviewSessionRepository extends JpaRepository<InterviewSessionEntity, UUID> {

    List<InterviewSessionEntity> findByCandidateNameOrderByDateTimeDesc(String candidateName);
}
