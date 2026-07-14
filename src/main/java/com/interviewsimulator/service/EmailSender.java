package com.interviewsimulator.service;

public interface EmailSender {

    /** {@code locale} is the requesting UI's language code ("az"/"tr"); null/blank/unknown falls back to "az". */
    void sendPasswordResetEmail(String toEmail, String resetLink, String locale);
}
