package com.interviewsimulator.service;

import com.interviewsimulator.dto.AdminQuestionDto;
import com.interviewsimulator.dto.AdminQuestionOptionDto;
import com.interviewsimulator.dto.AuditLogDto;
import com.interviewsimulator.dto.PagedResponse;
import com.interviewsimulator.dto.QuestionUpsertRequest;
import com.interviewsimulator.dto.UserSummaryDto;
import com.interviewsimulator.entity.AdminAuditLogEntity;
import com.interviewsimulator.entity.QuestionEntity;
import com.interviewsimulator.entity.UserEntity;
import com.interviewsimulator.repository.AdminAuditLogRepository;
import com.interviewsimulator.repository.QuestionRepository;
import com.interviewsimulator.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class AdminService {

    private static final Set<String> VALID_ROLES = Set.of("USER", "ADMIN");
    private static final Set<String> VALID_DIFFICULTIES = Set.of("EASY", "MEDIUM", "HARD");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final int MAX_PAGE_SIZE = 100;

    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final AdminAuditLogRepository auditLogRepository;

    public AdminService(UserRepository userRepository, QuestionRepository questionRepository,
                         AdminAuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public PagedResponse<UserSummaryDto> listUsers(String search, int page, int size) {
        Pageable pageable = pageable(page, size, Sort.by("createdAt").ascending());
        Page<UserEntity> users = search == null || search.isBlank()
                ? userRepository.findAll(pageable)
                : userRepository.findByEmailContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(search, search, pageable);
        return toPagedResponse(users.map(u -> new UserSummaryDto(u.getId().toString(), u.getEmail(),
                u.getDisplayName(), u.getRole(), u.getCreatedAt().format(DATE_FORMAT))));
    }

    @Transactional
    public UserSummaryDto changeRole(UUID adminId, UUID targetUserId, String role) {
        String newRole = role.trim().toUpperCase();
        if (!VALID_ROLES.contains(newRole)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol yalnız USER və ya ADMIN ola bilər");
        }
        if (adminId.equals(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Öz rolunuzu dəyişə bilməzsiniz");
        }
        UserEntity target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "İstifadəçi tapılmadı"));
        String oldRole = target.getRole();
        target.setRole(newRole);
        userRepository.save(target);
        audit(adminId, "ROLE_CHANGED", "USER", targetUserId, target.getEmail() + ": " + oldRole + " → " + newRole);
        return new UserSummaryDto(target.getId().toString(), target.getEmail(), target.getDisplayName(),
                target.getRole(), target.getCreatedAt().format(DATE_FORMAT));
    }

    @Transactional(readOnly = true)
    public PagedResponse<AdminQuestionDto> listQuestions(String search, int page, int size) {
        Pageable pageable = pageable(page, size, Sort.by("topic").ascending());
        Page<QuestionEntity> questions = search == null || search.isBlank()
                ? questionRepository.findAll(pageable)
                : questionRepository.search(search, pageable);
        return toPagedResponse(questions.map(this::toAdminDto));
    }

    @Transactional
    public AdminQuestionDto createQuestion(UUID adminId, QuestionUpsertRequest request) {
        validateQuestion(request);
        QuestionEntity question = new QuestionEntity(request.topic().trim(), request.text().trim(),
                request.difficulty().trim().toUpperCase());
        question.replaceOptions(request.options(), request.correctIndex());
        questionRepository.save(question);
        audit(adminId, "QUESTION_CREATED", "QUESTION", question.getId(), truncate(request.text()));
        return toAdminDto(question);
    }

    @Transactional
    public AdminQuestionDto updateQuestion(UUID adminId, UUID questionId, QuestionUpsertRequest request) {
        validateQuestion(request);
        QuestionEntity question = findQuestion(questionId);
        question.updateContent(request.topic().trim(), request.text().trim(), request.difficulty().trim().toUpperCase());
        question.replaceOptions(request.options(), request.correctIndex());
        questionRepository.save(question);
        audit(adminId, "QUESTION_UPDATED", "QUESTION", questionId, truncate(request.text()));
        return toAdminDto(question);
    }

    @Transactional
    public void deactivateQuestion(UUID adminId, UUID questionId) {
        QuestionEntity question = findQuestion(questionId);
        question.setActive(false);
        questionRepository.save(question);
        audit(adminId, "QUESTION_DEACTIVATED", "QUESTION", questionId, truncate(question.getText()));
    }

    @Transactional
    public AdminQuestionDto restoreQuestion(UUID adminId, UUID questionId) {
        QuestionEntity question = findQuestion(questionId);
        question.setActive(true);
        questionRepository.save(question);
        audit(adminId, "QUESTION_RESTORED", "QUESTION", questionId, truncate(question.getText()));
        return toAdminDto(question);
    }

    @Transactional(readOnly = true)
    public PagedResponse<AuditLogDto> listAudit(int page, int size) {
        Page<AdminAuditLogEntity> logs = auditLogRepository.findAllByOrderByCreatedAtDesc(
                pageable(page, size, Sort.unsorted()));
        return toPagedResponse(logs.map(l -> new AuditLogDto(l.getId().toString(), l.getAdmin().getEmail(),
                l.getAction(), l.getTargetType(), l.getTargetId() == null ? null : l.getTargetId().toString(),
                l.getDetails(), l.getCreatedAt().format(DATE_FORMAT))));
    }

    private void validateQuestion(QuestionUpsertRequest request) {
        if (!VALID_DIFFICULTIES.contains(request.difficulty().trim().toUpperCase())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Çətinlik yalnız EASY, MEDIUM və ya HARD ola bilər");
        }
        if (request.correctIndex() < 0 || request.correctIndex() >= request.options().size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "correctIndex variant sayı ilə uyğun deyil");
        }
    }

    private QuestionEntity findQuestion(UUID questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sual tapılmadı"));
    }

    private void audit(UUID adminId, String action, String targetType, UUID targetId, String details) {
        auditLogRepository.save(new AdminAuditLogEntity(userRepository.getReferenceById(adminId),
                action, targetType, targetId, details));
    }

    private AdminQuestionDto toAdminDto(QuestionEntity question) {
        List<AdminQuestionOptionDto> options = new ArrayList<>();
        for (int i = 0; i < question.getOptions().size(); i++) {
            var option = question.getOptions().get(i);
            options.add(new AdminQuestionOptionDto(i, option.getOptionText(), Boolean.TRUE.equals(option.getCorrect())));
        }
        return new AdminQuestionDto(question.getId().toString(), question.getTopic(), question.getText(),
                question.getDifficulty(), Boolean.TRUE.equals(question.getActive()), options);
    }

    private Pageable pageable(int page, int size, Sort sort) {
        return PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), MAX_PAGE_SIZE), sort);
    }

    private <T> PagedResponse<T> toPagedResponse(Page<T> page) {
        return new PagedResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }

    private String truncate(String text) {
        return text.length() > 200 ? text.substring(0, 200) + "…" : text;
    }
}
