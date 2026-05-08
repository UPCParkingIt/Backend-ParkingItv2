package com.parkingit.cloud.notifications.application.internal.inboundservices.email;

import com.parkingit.cloud.notifications.domain.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String sender;

    @Override
    public String sendEmail(String recipient, String subject, String messageBody) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(sender);
            message.setTo(recipient);
            message.setText(messageBody);
            message.setSubject(subject);

            mailSender.send(message);
            return "Email sent successfully";
        } catch (Exception e) {
            log.error("[EmailService] Error sending email to {}: {}", recipient, e.getMessage(), e);
            return "Error while sending email: " + e.getMessage();
        }
    }

    @Override
    public String sendEmailWithAttachment(String recipient, String subject, String messageBody, String attachmentPath) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper;

        try {
            helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom(sender);
            helper.setTo(recipient);
            helper.setText(messageBody);
            helper.setSubject(subject);

            FileSystemResource file = new FileSystemResource(new File(attachmentPath));

            if (!file.exists()) {
                log.error("[EmailService] Attachment file not found: {}", attachmentPath);
                throw new IllegalArgumentException("Attachment file not found: " + attachmentPath);
            }

            helper.addAttachment(file.getFilename(), file);

            mailSender.send(mimeMessage);
            return "Email sent successfully";
        } catch (MessagingException e) {
            log.error("[EmailService] Error sending email with attachment to {}: {}", recipient, e.getMessage(), e);
            return "Error while sending email: " + e.getMessage();
        } catch (Exception e) {
            log.error("[EmailService] Unexpected error sending email with attachment to {}: {}", recipient, e.getMessage(), e);
            return "Error while sending email: " + e.getMessage();
        }
    }
}
