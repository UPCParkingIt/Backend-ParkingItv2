package com.parkingit.cloud.notifications.interfaces.rest;

import com.parkingit.cloud.notifications.domain.services.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@CrossOrigin(origins = "*", methods = { RequestMethod.POST, RequestMethod.GET, RequestMethod.PUT, RequestMethod.DELETE })
@RestController
@RequestMapping(value = "/api/v1/emails", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Emails", description = "Email Management - Send notifications via email")
public class EmailsController {
    @Autowired
    private EmailService emailService;

    /**
     * Sends a plain text email to the specified recipient.
     * Used for password recovery, notifications, and alerts.
     *
     * @param recipient the email address of the recipient
     * @param subject the email subject line
     * @param messageBody the email body content
     * @return success or error message
     */
    @PostMapping("/send")
    @Operation(summary = "Send email", description = "Sends a plain text email to the specified recipient with subject and message body")
    @ApiResponse(responseCode = "200", description = "Email sent successfully")
    @ApiResponse(responseCode = "400", description = "Invalid email address or parameters")
    public String sendMail(@RequestParam String recipient, @RequestParam String subject, @RequestParam String messageBody) {
        return emailService.sendEmail(recipient, subject, messageBody);
    }

    /**
     * Sends an email with a file attachment to the specified recipient.
     * Used for sending invoices, receipts, and parking reports.
     *
     * @param recipient the email address of the recipient
     * @param subject the email subject line
     * @param messageBody the email body content
     * @param attachmentPath the file path of the attachment to include
     * @return success or error message
     */
    @PostMapping("/send-attachment")
    @Operation(summary = "Send email with attachment", description = "Sends an email with a file attachment (PDF, document, etc). Used for invoices and reports")
    @ApiResponse(responseCode = "200", description = "Email with attachment sent successfully")
    @ApiResponse(responseCode = "400", description = "Invalid parameters or file not found")
    public String sendMailWithAttachment(@RequestParam String recipient, @RequestParam String subject, @RequestParam String messageBody, @RequestParam String attachmentPath) {
        return emailService.sendEmailWithAttachment(recipient, subject, messageBody, attachmentPath);
    }
}
