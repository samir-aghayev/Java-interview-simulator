package com.interviewsimulator.web;

import com.interviewsimulator.dto.SubjectTopicsDto;
import com.interviewsimulator.repository.QuestionRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MetaController {

    private final QuestionRepository questionRepository;

    public MetaController(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @GetMapping("/api/meta/topics")
    @Transactional(readOnly = true)
    public List<SubjectTopicsDto> topics() {
        Map<String, List<String>> bySubject = new LinkedHashMap<>();
        for (Object[] row : questionRepository.findActiveSubjectTopics()) {
            bySubject.computeIfAbsent((String) row[0], k -> new ArrayList<>()).add((String) row[1]);
        }
        List<SubjectTopicsDto> result = new ArrayList<>();
        bySubject.forEach((subject, topics) -> result.add(new SubjectTopicsDto(subject, topics)));
        return result;
    }
}
