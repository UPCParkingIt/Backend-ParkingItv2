package com.parkingit.edge.sync.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SyncStatisticsDTO {
    private long pendingSyncs;
    private long syncedSyncs;
    private long failedSyncs;
    private long totalSyncs;
    private double successRate;
    private LocalDateTime lastCheck;
    private String cloudStatus;  // OK, UNREACHABLE
}
