package com.interviewsimulator.web;

import com.interviewsimulator.dto.AnswerSubmissionDto;
import com.interviewsimulator.dto.GradeResponse;
import com.interviewsimulator.dto.ProgressResponse;
import com.interviewsimulator.dto.QuizStartRequest;
import com.interviewsimulator.dto.QuizStartResponse;
import com.interviewsimulator.dto.QuizSubmitRequest;
import com.interviewsimulator.dto.WeakTopicsResponse;
import com.interviewsimulator.security.AuthenticatedUser;
import com.interviewsimulator.service.InterviewService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class QuizController {

    private static final int DEFAULT_QUESTION_COUNT = 10;

    private final InterviewService interviewService;

    public QuizController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @PostMapping("/api/quiz/start")
    public QuizStartResponse start(@AuthenticationPrincipal AuthenticatedUser principal, @RequestBody QuizStartRequest request) {
        int count = request.questionCount() != null ? Math.max(0, request.questionCount()) : DEFAULT_QUESTION_COUNT;
        return new QuizStartResponse(interviewService.pickRandomQuestions(principal.id(), count,
                request.subject(), request.topics(), request.locale()));
    }

    @PostMapping("/api/quiz/submit")
    public GradeResponse submit(@AuthenticationPrincipal AuthenticatedUser principal, @RequestBody QuizSubmitRequest request) {
        List<AnswerSubmissionDto> answers = request.answers() != null ? request.answers() : List.of();
        return interviewService.grade(principal.id(), answers, request.locale());
    }

    @GetMapping("/api/stats/weak")
    public WeakTopicsResponse weak(@AuthenticationPrincipal AuthenticatedUser principal) {
        return interviewService.weakTopicsSummary(principal.id());
    }

    @GetMapping("/api/stats/progress")
    public ProgressResponse progress(@AuthenticationPrincipal AuthenticatedUser principal) {
        return interviewService.progressSummary(principal.id());
    }
}
