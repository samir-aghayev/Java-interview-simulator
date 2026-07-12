package com.interviewsimulator.repository;

import com.interviewsimulator.entity.QuestionTranslationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface QuestionTranslationRepository extends JpaRepository<QuestionTranslationEntity, UUID> {

    List<QuestionTranslationEntity> findByLocaleAndQuestion_IdIn(String locale, Collection<UUID> questionIds);
}
