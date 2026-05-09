package com.parkingit.edge.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.parkingit.edge.sync.persistence.entities.EdgeSyncStatus;
import com.parkingit.edge.sync.persistence.repositories.EdgeSyncStatusRepository;
import com.parkingit.edge.sync.dto.SyncStatisticsDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class EdgeSyncService {

    private final EdgeSyncStatusRepository syncStatusRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${edge.cloud.url}")
    private String cloudUrl;

    @Value("${edge.device-id}")
    private String deviceId;

    @Value("${edge.sync.max-retries:3}")
    private int maxRetries;

    private boolean cloudAvailable = true;

    public EdgeSyncService(
            EdgeSyncStatusRepository syncStatusRepository,
            RestTemplate restTemplate
    ) {
        this.syncStatusRepository = syncStatusRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Registra una nueva sincronización pendiente
     */
    public void registerPendingSync(
            String syncType,
            UUID entityId,
            String entityType,
            Object payload
    ) {
        try {
            EdgeSyncStatus syncStatus = EdgeSyncStatus.builder()
                .syncType(syncType)
                .entityId(entityId)
                .entityType(entityType)
                .payload(payload)
                .status("PENDING")
                .retryCount(0)
                .maxRetries(maxRetries)
                .build();

            syncStatusRepository.save(syncStatus);
            log.info("Registered pending sync: {} for entity {} ({})",
                syncType, entityId, entityType);

        } catch (Exception e) {
            log.error("Error registering pending sync", e);
        }
    }

    /**
     * Ejecuta sincronización pendiente cada X segundos
     */
    @Scheduled(fixedDelayString = "${edge.sync.interval:10000}")
    public void processPendingSyncs() {
        try {
            List<EdgeSyncStatus> pendingSyncs = syncStatusRepository.findPendingSyncs();

            if (pendingSyncs.isEmpty()) {
                log.debug("No pending syncs to process");
                return;
            }

            log.info("Processing {} pending syncs (cloudAvailable: {})",
                pendingSyncs.size(), cloudAvailable);

            // Verificar disponibilidad de Cloud
            if (!cloudAvailable) {
                checkCloudAvailability();
                if (!cloudAvailable) {
                    log.warn("Cloud service is unreachable. Syncs will retry later.");
                    return;
                }
            }

            for (EdgeSyncStatus sync : pendingSyncs) {
                processSingleSync(sync);
            }

        } catch (Exception e) {
            log.error("Error processing pending syncs: {}", e.getMessage(), e);
        }
    }

    /**
     * Procesa una sincronización individual
     */
    private void processSingleSync(EdgeSyncStatus sync) {
        try {
            sync.markAsSyncing();
            syncStatusRepository.save(sync);

            // Enviar a Cloud según tipo de sincronización
            switch (sync.getSyncType()) {
                case "VEHICLE_ENTRY" -> syncVehicleEntry(sync);
                case "VEHICLE_EXIT" -> syncVehicleExit(sync);
                case "PARKING_UPDATE" -> syncParkingUpdate(sync);
                default -> {
                    log.warn("Unknown sync type: {}", sync.getSyncType());
                    sync.markAsFailed("Unknown sync type: " + sync.getSyncType());
                    syncStatusRepository.save(sync);
                }
            }

        } catch (RestClientException e) {
            log.warn("Cloud not reachable, will retry later: {}", e.getMessage());
            cloudAvailable = false;
            sync.markAsFailed("Cloud service unreachable: " + e.getMessage());

            if (sync.shouldRetry()) {
                syncStatusRepository.save(sync);
            }

        } catch (Exception e) {
            log.warn("Failed to sync {}: {}", sync.getId(), e.getMessage());
            sync.markAsFailed(e.getMessage());

            if (sync.shouldRetry()) {
                syncStatusRepository.save(sync);
                log.info("Scheduled retry for {}: attempt {}/{}",
                    sync.getId(), sync.getRetryCount(), sync.getMaxRetries());
            } else {
                sync.setStatus("FAILED");
                syncStatusRepository.save(sync);
                log.error("Sync {} failed permanently after {} attempts",
                    sync.getId(), sync.getRetryCount());
            }
        }
    }

    private void syncVehicleEntry(EdgeSyncStatus sync) {
        try {
            String response = restTemplate.postForObject(
                cloudUrl + "/api/v1/edge/vehicle-entries",
                sync.getPayload(),
                String.class
            );
            log.info("Vehicle entry synced successfully: {}", response);
            sync.markAsSynced();
            cloudAvailable = true;
            syncStatusRepository.save(sync);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sync vehicle entry", e);
        }
    }

    private void syncVehicleExit(EdgeSyncStatus sync) {
        try {
            String response = restTemplate.postForObject(
                cloudUrl + "/api/v1/edge/vehicle-exits",
                sync.getPayload(),
                String.class
            );
            log.info("Vehicle exit synced successfully: {}", response);
            sync.markAsSynced();
            cloudAvailable = true;
            syncStatusRepository.save(sync);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sync vehicle exit", e);
        }
    }

    private void syncParkingUpdate(EdgeSyncStatus sync) {
        try {
            String response = restTemplate.postForObject(
                cloudUrl + "/api/v1/edge/parking/update",
                sync.getPayload(),
                String.class
            );
            log.info("Parking update synced successfully: {}", response);
            sync.markAsSynced();
            cloudAvailable = true;
            syncStatusRepository.save(sync);
        } catch (Exception e) {
            throw new RuntimeException("Failed to sync parking update", e);
        }
    }

    /**
     * Verifica disponibilidad de Cloud
     */
    private void checkCloudAvailability() {
        try {
            restTemplate.getForObject(cloudUrl + "/api/v1/health", String.class);
            cloudAvailable = true;
            log.info("Cloud service is now available");
        } catch (Exception e) {
            cloudAvailable = false;
            log.debug("Cloud service check failed: {}", e.getMessage());
        }
    }

    /**
     * Obtener estado de sincronización
     */
    public EdgeSyncStatus getSyncStatus(UUID syncId) {
        return syncStatusRepository.findById(syncId)
            .orElseThrow(() -> new RuntimeException("Sync not found: " + syncId));
    }

    /**
     * Obtener estadísticas de sincronización
     */
    public SyncStatisticsDTO getSyncStatistics() {
        long pending = syncStatusRepository.countByStatus("PENDING");
        long synced = syncStatusRepository.countByStatus("SYNCED");
        long failed = syncStatusRepository.countByStatus("FAILED");
        long total = pending + synced + failed;

        double successRate = total > 0 ? (synced * 100.0) / total : 0;

        return SyncStatisticsDTO.builder()
            .pendingSyncs(pending)
            .syncedSyncs(synced)
            .failedSyncs(failed)
            .totalSyncs(total)
            .successRate(successRate)
            .lastCheck(LocalDateTime.now())
            .cloudStatus(cloudAvailable ? "OK" : "UNREACHABLE")
            .build();
    }

    /**
     * Retornar si Cloud está disponible
     */
    public boolean isCloudAvailable() {
        checkCloudAvailability();
        return cloudAvailable;
    }
}

