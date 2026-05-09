package com.parkingit.edge.sync;

import com.parkingit.shared.domain.events.VehicleEnteredEvent;
import com.parkingit.shared.domain.events.VehicleExitedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CloudSyncService {

    @Value("${edge.cloud.url:http://localhost:8080}")
    private String cloudUrl;

    @Value("${edge.device-id:edge-device-1}")
    private String edgeDeviceId;

    private final WebClient webClient;
    private final List<VehicleEnteredEvent> entryBuffer = new ArrayList<>();
    private final List<VehicleExitedEvent> exitBuffer = new ArrayList<>();

    public CloudSyncService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public void bufferEntryEvent(VehicleEnteredEvent event) {
        synchronized (entryBuffer) {
            entryBuffer.add(event);
            log.info("Buffered vehicle entry event: {}", event.getLicensePlate());
        }
    }

    public void bufferExitEvent(VehicleExitedEvent event) {
        synchronized (exitBuffer) {
            exitBuffer.add(event);
            log.info("Buffered vehicle exit event: {}", event.getLicensePlate());
        }
    }

    @Scheduled(fixedDelayString = "${edge.sync.interval:10000}")
    public void syncWithCloud() {
        log.debug("Syncing with cloud...");
        syncEntries();
        syncExits();
    }

    private void syncEntries() {
        synchronized (entryBuffer) {
            if (entryBuffer.isEmpty()) return;

            try {
                // TODO: Implementar POST a cloud endpoint
                log.info("Syncing {} entry events to cloud", entryBuffer.size());
                entryBuffer.clear();
            } catch (Exception e) {
                log.warn("Failed to sync entries: {}", e.getMessage());
            }
        }
    }

    private void syncExits() {
        synchronized (exitBuffer) {
            if (exitBuffer.isEmpty()) return;

            try {
                // TODO: Implementar POST a cloud endpoint
                log.info("Syncing {} exit events to cloud", exitBuffer.size());
                exitBuffer.clear();
            } catch (Exception e) {
                log.warn("Failed to sync exits: {}", e.getMessage());
            }
        }
    }
}