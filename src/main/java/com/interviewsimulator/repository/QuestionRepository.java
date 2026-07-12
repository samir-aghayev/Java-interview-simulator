package com.interviewsimulator.repository;

import com.interviewsimulator.entity.QuestionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<QuestionEntity, UUID> {

    @Query("select q from QuestionEntity q where q.active = true and q.id not in "
            + "(select m.question.id from MasteredQuestionEntity m where m.user.id = :userId)")
    List<QuestionEntity> findAvailableForUser(UUID userId);

    @Query("select q from QuestionEntity q where lower(q.subject) like lower(concat('%', :search, '%')) "
            + "or lower(q.topic) like lower(concat('%', :search, '%')) "
            + "or lower(q.text) like lower(concat('%', :search, '%'))")
    Page<QuestionEntity> search(String search, Pageable pageable);

    @Query("select q.subject, q.topic from QuestionEntity q where q.active = true "
            + "group by q.subject, q.topic order by q.subject, q.topic")
    List<Object[]> findActiveSubjectTopics();
}
