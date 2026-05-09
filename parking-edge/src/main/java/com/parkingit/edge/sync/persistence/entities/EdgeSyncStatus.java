package com.parkingit.edge.sync.persistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "edge_sync_status", indexes = {
        @Index(name = "idx_sync_pending", columnList = "status,next_retry_at"),
        @Index(name = "idx_sync_entity", columnList = "entity_id,entity_type")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EdgeSyncStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String syncType;  // 'VEHICLE_ENTRY', 'VEHICLE_EXIT'

    @Column(nullable = false)
    private UUID entityId;

    @Column(nullable = false, length = 50)
    private String entityType;  // 'VehicleEntry', 'ExtraUserRegistration'

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String status = "PENDING";  // PENDING, SYNCING, SYNCED, FAILED

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Object payload;  // Datos JSON a sincronizar

    private LocalDateTime lastAttemptAt;
    private LocalDateTime nextRetryAt;
    private String errorMessage;

    @Builder.Default
    private Integer retryCount = 0;

    @Builder.Default
    private Integer maxRetries = 3;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean shouldRetry() {
        return retryCount < maxRetries &&
                (nextRetryAt == null || LocalDateTime.now().isAfter(nextRetryAt));
    }

    public void markAsSyncing() {
        this.status = "SYNCING";
        this.lastAttemptAt = LocalDateTime.now();
    }

    public void markAsSynced() {
        this.status = "SYNCED";
        this.retryCount = 0;
        this.errorMessage = null;
    }

    public void markAsFailed(String error) {
        this.status = "FAILED";
        this.errorMessage = error;
        this.retryCount++;
        this.nextRetryAt = LocalDateTime.now().plusSeconds(5 * retryCount);  // Exponential backoff
    }
}