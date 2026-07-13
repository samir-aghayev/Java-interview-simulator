package com.interviewsimulator.service;

public interface EmailSender {

    void sendPasswordResetEmail(String toEmail, String resetLink);
}
