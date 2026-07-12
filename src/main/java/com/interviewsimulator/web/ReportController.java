package com.interviewsimulator.web;

import com.interviewsimulator.dto.ReportCreateRequest;
import com.interviewsimulator.entity.QuestionEntity;
import com.interviewsimulator.entity.QuestionReportEntity;
import com.interviewsimulator.repository.QuestionReportRepository;
import com.interviewsimulator.repository.QuestionRepository;
import com.interviewsimulator.repository.UserRepository;
import com.interviewsimulator.security.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
public class ReportController {

    private final QuestionRepository questionRepository;
    private final QuestionReportRepository reportRepository;
    private final UserRepository userRepository;

    public ReportController(QuestionRepository questionRepository, QuestionReportRepository reportRepository,
                             UserRepository userRepository) {
        this.questionRepository = questionRepository;
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/api/reports")
    @Transactional
    public Map<String, String> create(@AuthenticationPrincipal AuthenticatedUser principal,
                                       @Valid @RequestBody ReportCreateRequest request) {
        QuestionEntity question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sual tapılmadı"));
        QuestionReportEntity report = new QuestionReportEntity(question,
                userRepository.getReferenceById(principal.id()), request.message().trim());
        reportRepository.save(report);
        return Map.of("id", report.getId().toString(), "status", report.getStatus());
    }
}
