package com.parkingit.cloud.notifications.domain.services;

public interface EmailService {
    String sendEmail(String recipient, String subject, String messageBody);
    String sendEmailWithAttachment(String recipient, String subject, String messageBody, String attachmentPath);
}
