package com.parkingit.edge.sync.interfaces.rest;

import com.parkingit.edge.sync.EdgeSyncService;
import com.parkingit.edge.sync.dto.SyncStatisticsDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/edge")
@Tag(name = "Edge Status", description = "Edge Device Status and Monitoring")
public class EdgeStatusController {

    private final EdgeSyncService edgeSyncService;

    /**
     * Health check del Edge Device
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if edge service is running")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("service", "parking-edge-service");
        response.put("timestamp", LocalDateTime.now());
        response.put("cloudAvailable", edgeSyncService.isCloudAvailable());

        return ResponseEntity.ok(response);
    }

    /**
     * Estado de sincronización del Edge
     */
    @GetMapping("/sync/status")
    @Operation(summary = "Sync status", description = "Get current synchronization status and statistics")
    public ResponseEntity<SyncStatisticsDTO> syncStatus() {
        try {
            SyncStatisticsDTO stats = edgeSyncService.getSyncStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting sync status", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Estadísticas detalladas del Edge (para dashboard admin)
     */
    @GetMapping("/stats")
    @Operation(summary = "Edge statistics", description = "Get detailed edge device statistics")
    public ResponseEntity<Map<String, Object>> edgeStatistics() {
        try {
            SyncStatisticsDTO syncStats = edgeSyncService.getSyncStatistics();

            Map<String, Object> response = new HashMap<>();
            response.put("timestamp", LocalDateTime.now());
            response.put("cloudStatus", syncStats.getCloudStatus());
            response.put("syncStatistics", new HashMap<String, Object>() {{
                put("pending", syncStats.getPendingSyncs());
                put("synced", syncStats.getSyncedSyncs());
                put("failed", syncStats.getFailedSyncs());
                put("total", syncStats.getTotalSyncs());
                put("successRate", String.format("%.2f%%", syncStats.getSuccessRate()));
            }});
            response.put("uptime", "N/A");  // TODO: implementar tracking de uptime
            response.put("version", "0.0.1-SNAPSHOT");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error getting edge statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

