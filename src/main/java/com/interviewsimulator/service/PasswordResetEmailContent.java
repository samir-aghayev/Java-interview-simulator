package com.interviewsimulator.service;

/** Subject/body text for the password-reset email, localized by UI language code. */
final class PasswordResetEmailContent {

    final String subject;
    final String body;

    private PasswordResetEmailContent(String subject, String body) {
        this.subject = subject;
        this.body = body;
    }

    static PasswordResetEmailContent forLocale(String locale, String resetLink) {
        if ("tr".equalsIgnoreCase(locale == null ? "" : locale.trim())) {
            return new PasswordResetEmailContent("Şifre sıfırlama",
                    "Şifrenizi sıfırlamak için linke tıklayın:\n\n" + resetLink
                            + "\n\nBu link 30 dakika geçerlidir. Bu talebi siz yapmadıysanız, bu e-postayı yok sayabilirsiniz.");
        }
        return new PasswordResetEmailContent("Şifrə bərpası",
                "Şifrənizi bərpa etmək üçün linkə keçin:\n\n" + resetLink
                        + "\n\nBu link 30 dəqiqə etibarlıdır. Bu tələbi siz etməmisinizsə, mesajı gözardı edin.");
    }
}
