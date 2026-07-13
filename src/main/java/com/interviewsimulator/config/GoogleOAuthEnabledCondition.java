package com.interviewsimulator.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.NonNull;

/**
 * True only once real Google OAuth credentials are configured (GOOGLE_CLIENT_ID /
 * GOOGLE_CLIENT_SECRET). Spring Boot's own oauth2 client autoconfiguration throws a hard
 * startup error on blank client-id/secret, so registration is built manually and gated by
 * this condition instead — the app starts cleanly with no credentials at all.
 */
public class GoogleOAuthEnabledCondition implements Condition {

    @Override
    public boolean matches(@NonNull ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
        String clientId = context.getEnvironment().getProperty("app.oauth2.google.client-id", "");
        String clientSecret = context.getEnvironment().getProperty("app.oauth2.google.client-secret", "");
        return !clientId.isBlank() && !clientSecret.isBlank();
    }
}
