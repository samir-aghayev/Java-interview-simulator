package com.interviewsimulator.repository;

import com.interviewsimulator.entity.QuestionOptionTranslationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface QuestionOptionTranslationRepository extends JpaRepository<QuestionOptionTranslationEntity, UUID> {

    List<QuestionOptionTranslationEntity> findByLocaleAndOption_IdIn(String locale, Collection<UUID> optionIds);
}
