package com.parkingit.edge.recognition.domain.model.entities;

import com.parkingit.edge.shared.domain.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.time.LocalDateTime;

@Entity
public class RecognitionProcess extends AuditableAbstractAggregateRoot<RecognitionProcess> {
    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    public RecognitionProcess() {
        this.isActive = false;
    }

    public void startProcess() {
        this.isActive = true;
        this.activatedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusSeconds(10);  // ← Expira en 10 segundos
    }

    public void stopProcess() {
        this.isActive = false;
    }

    public Boolean getIsActive() {
        if (isActive && expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
            this.isActive = false;
        }
        return isActive;
    }
}
