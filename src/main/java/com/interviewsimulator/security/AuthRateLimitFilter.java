package com.interviewsimulator.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Component
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 10;
    private static final long WINDOW_SECONDS = 60;

    private final ConcurrentHashMap<String, Deque<Instant>> attemptsByIp = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/api/auth/")) {
            String ip = request.getRemoteAddr();
            Deque<Instant> attempts = attemptsByIp.computeIfAbsent(ip, k -> new ConcurrentLinkedDeque<>());
            Instant now = Instant.now();
            synchronized (attempts) {
                while (!attempts.isEmpty() && attempts.peekFirst().isBefore(now.minusSeconds(WINDOW_SECONDS))) {
                    attempts.pollFirst();
                }
                if (attempts.size() >= MAX_ATTEMPTS) {
                    response.setStatus(429);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"error\":\"Çox tez-tez cəhd edildi, bir az sonra yenidən yoxlayın.\"}");
                    return;
                }
                attempts.addLast(now);
            }
        }
        filterChain.doFilter(request, response);
    }
}
