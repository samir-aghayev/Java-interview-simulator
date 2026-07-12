package com.interviewsimulator.web;

import com.interviewsimulator.dto.AdminQuestionDto;
import com.interviewsimulator.dto.AuditLogDto;
import com.interviewsimulator.dto.PagedResponse;
import com.interviewsimulator.dto.QuestionUpsertRequest;
import com.interviewsimulator.dto.ReportDto;
import com.interviewsimulator.dto.RoleUpdateRequest;
import com.interviewsimulator.dto.UserSummaryDto;
import com.interviewsimulator.security.AuthenticatedUser;
import com.interviewsimulator.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public PagedResponse<UserSummaryDto> listUsers(@RequestParam(defaultValue = "") String search,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        return adminService.listUsers(search, page, size);
    }

    @PatchMapping("/users/{id}/role")
    public UserSummaryDto changeRole(@AuthenticationPrincipal AuthenticatedUser principal,
                                      @PathVariable UUID id,
                                      @Valid @RequestBody RoleUpdateRequest request) {
        return adminService.changeRole(principal.id(), id, request.role());
    }

    @GetMapping("/questions")
    public PagedResponse<AdminQuestionDto> listQuestions(@RequestParam(defaultValue = "") String search,
                                                          @RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size) {
        return adminService.listQuestions(search, page, size);
    }

    @PostMapping("/questions")
    public AdminQuestionDto createQuestion(@AuthenticationPrincipal AuthenticatedUser principal,
                                            @Valid @RequestBody QuestionUpsertRequest request) {
        return adminService.createQuestion(principal.id(), request);
    }

    @PutMapping("/questions/{id}")
    public AdminQuestionDto updateQuestion(@AuthenticationPrincipal AuthenticatedUser principal,
                                            @PathVariable UUID id,
                                            @Valid @RequestBody QuestionUpsertRequest request) {
        return adminService.updateQuestion(principal.id(), id, request);
    }

    @DeleteMapping("/questions/{id}")
    public void deactivateQuestion(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable UUID id) {
        adminService.deactivateQuestion(principal.id(), id);
    }

    @PostMapping("/questions/{id}/restore")
    public AdminQuestionDto restoreQuestion(@AuthenticationPrincipal AuthenticatedUser principal, @PathVariable UUID id) {
        return adminService.restoreQuestion(principal.id(), id);
    }

    @GetMapping("/reports")
    public PagedResponse<ReportDto> listReports(@RequestParam(defaultValue = "") String status,
                                                 @RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return adminService.listReports(status, page, size);
    }

    @PatchMapping("/reports/{id}")
    public ReportDto resolveReport(@AuthenticationPrincipal AuthenticatedUser principal,
                                    @PathVariable UUID id,
                                    @RequestBody java.util.Map<String, String> body) {
        return adminService.resolveReport(principal.id(), id, body.get("status"));
    }

    @GetMapping("/audit")
    public PagedResponse<AuditLogDto> listAudit(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return adminService.listAudit(page, size);
    }
}
