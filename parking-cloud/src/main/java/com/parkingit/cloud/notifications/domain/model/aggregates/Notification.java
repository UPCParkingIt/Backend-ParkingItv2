package com.parkingit.cloud.notifications.domain.model.aggregates;

import com.parkingit.cloud.shared.domain.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
public class Notification extends AuditableAbstractAggregateRoot<Notification> {
    @Column(name = "recipient_user_id", nullable = false)
    @NotNull
    private UUID recipientUserId;

    @Column(length = 500)
    @NotNull
    private String subject;

    @Column(columnDefinition = "TEXT")
    @NotNull
    private String messageBody;

    public Notification(
            UUID recipientUserId,
            String subject,
            String messageBody
    ) {
        this.recipientUserId = recipientUserId;
        this.subject = subject;
        this.messageBody = messageBody;
    }

    public Notification() {}
}
