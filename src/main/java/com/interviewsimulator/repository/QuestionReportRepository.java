package com.interviewsimulator.repository;

import com.interviewsimulator.entity.QuestionReportEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface QuestionReportRepository extends JpaRepository<QuestionReportEntity, UUID> {

    Page<QuestionReportEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<QuestionReportEntity> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);
}
