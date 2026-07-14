package com.interviewsimulator.service;

import com.interviewsimulator.dto.AdminQuestionDto;
import com.interviewsimulator.dto.QuestionUpsertRequest;
import com.interviewsimulator.dto.UserSummaryDto;
import com.interviewsimulator.entity.QuestionEntity;
import com.interviewsimulator.entity.QuestionReportEntity;
import com.interviewsimulator.entity.UserEntity;
import com.interviewsimulator.repository.AdminAuditLogRepository;
import com.interviewsimulator.repository.InterviewSessionRepository;
import com.interviewsimulator.repository.QuestionReportRepository;
import com.interviewsimulator.repository.QuestionRepository;
import com.interviewsimulator.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private AdminAuditLogRepository auditLogRepository;
    @Mock
    private QuestionReportRepository reportRepository;
    @Mock
    private InterviewSessionRepository sessionRepository;

    private AdminService adminService;
    private UUID adminId;

    @BeforeEach
    void setUp() {
        adminService = new AdminService(userRepository, questionRepository, auditLogRepository, reportRepository,
                sessionRepository);
        adminId = UUID.randomUUID();
    }

    @Test
    void changeRoleRejectsInvalidRole() {
        UUID targetId = UUID.randomUUID();
        assertThatThrownBy(() -> adminService.changeRole(adminId, targetId, "SUPERUSER"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("USER");
        verify(userRepository, never()).save(any());
    }

    @Test
    void changeRoleRejectsSelfDemotion() {
        assertThatThrownBy(() -> adminService.changeRole(adminId, adminId, "USER"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("rolunuzu");
    }

    @Test
    void changeRoleUpdatesTargetAndWritesAudit() {
        UUID targetId = UUID.randomUUID();
        UserEntity target = new UserEntity("target@example.com", "hash", "Target User", "USER");
        when(userRepository.findById(targetId)).thenReturn(Optional.of(target));
        when(userRepository.getReferenceById(adminId)).thenReturn(new UserEntity("admin@example.com", "h", "Admin", "ADMIN"));

        UserSummaryDto result = adminService.changeRole(adminId, targetId, "admin");

        assertThat(result.role()).isEqualTo("ADMIN");
        verify(userRepository).save(target);
        verify(auditLogRepository).save(any());
    }

    @Test
    void changeRoleThrowsWhenTargetMissing() {
        UUID targetId = UUID.randomUUID();
        when(userRepository.findById(targetId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminService.changeRole(adminId, targetId, "ADMIN"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("tapılmadı");
    }

    private QuestionUpsertRequest validRequest() {
        return new QuestionUpsertRequest("Java", "OOP", "Sual?", "EASY", List.of("A", "B", "C"), 1);
    }

    @Test
    void createQuestionRejectsInvalidDifficulty() {
        QuestionUpsertRequest request = new QuestionUpsertRequest("Java", "OOP", "Sual?", "IMPOSSIBLE",
                List.of("A", "B"), 0);
        assertThatThrownBy(() -> adminService.createQuestion(adminId, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Çətinlik");
        verify(questionRepository, never()).save(any());
    }

    @Test
    void createQuestionRejectsOutOfRangeCorrectIndex() {
        QuestionUpsertRequest request = new QuestionUpsertRequest("Java", "OOP", "Sual?", "EASY",
                List.of("A", "B"), 5);
        assertThatThrownBy(() -> adminService.createQuestion(adminId, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("correctIndex");
    }

    @Test
    void createQuestionSavesAndAudits() {
        when(userRepository.getReferenceById(adminId)).thenReturn(new UserEntity("admin@example.com", "h", "Admin", "ADMIN"));

        AdminQuestionDto result = adminService.createQuestion(adminId, validRequest());

        assertThat(result.topic()).isEqualTo("OOP");
        assertThat(result.options()).hasSize(3);
        assertThat(result.options().get(1).correct()).isTrue();
        verify(questionRepository).save(any(QuestionEntity.class));
        verify(auditLogRepository).save(any());
    }

    @Test
    void resolveReportRejectsInvalidStatus() {
        UUID reportId = UUID.randomUUID();
        assertThatThrownBy(() -> adminService.resolveReport(adminId, reportId, "OPEN"))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("RESOLVED");
        verify(reportRepository, never()).findById(any());
    }

    @Test
    void resolveReportUpdatesStatusAndAudits() {
        UUID reportId = UUID.randomUUID();
        QuestionEntity question = new QuestionEntity("Java", "OOP", "Sual?", "EASY");
        question.replaceOptions(List.of("A", "B"), 0);
        UserEntity reporter = new UserEntity("user@example.com", "h", "User", "USER");
        QuestionReportEntity report = new QuestionReportEntity(question, reporter, "Bu sualda səhv var");
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(userRepository.getReferenceById(adminId)).thenReturn(new UserEntity("admin@example.com", "h", "Admin", "ADMIN"));

        var result = adminService.resolveReport(adminId, reportId, "resolved");

        assertThat(result.status()).isEqualTo("RESOLVED");
        verify(reportRepository).save(report);
        verify(auditLogRepository).save(any());
    }
}
