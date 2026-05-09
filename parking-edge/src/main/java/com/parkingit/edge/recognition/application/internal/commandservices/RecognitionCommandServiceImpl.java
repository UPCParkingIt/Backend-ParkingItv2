package com.parkingit.edge.recognition.application.internal.commandservices;

import com.parkingit.edge.recognition.domain.model.commands.ActivateRecognition;
import com.parkingit.edge.recognition.domain.model.commands.ActivateRecognitionCommand;
import com.parkingit.edge.recognition.domain.model.commands.DeactivateRecognitionCommand;
import com.parkingit.edge.recognition.domain.model.entities.RecognitionProcess;
import com.parkingit.edge.recognition.domain.model.entities.RecognitionSession;
import com.parkingit.edge.recognition.domain.model.events.RecognitionActivatedEvent;
import com.parkingit.edge.recognition.domain.model.events.RecognitionDeactivatedEvent;
import com.parkingit.edge.recognition.domain.model.queries.GetLatestStatusQuery;
import com.parkingit.edge.recognition.domain.model.valueobjects.RecognitionStatus;
import com.parkingit.edge.recognition.domain.services.RecognitionManagementService;
import com.parkingit.edge.recognition.infrastructure.persistence.jpa.repositories.RecognitionProcessRepository;
import com.parkingit.edge.recognition.infrastructure.persistence.jpa.repositories.RecognitionSessionRepository;
import com.parkingit.edge.sync.EdgeSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecognitionCommandServiceImpl implements RecognitionManagementService {
    private final RecognitionSessionRepository sessionRepository;
    private final RecognitionProcessRepository processRepository;
    private final EdgeSyncService edgeSyncService;
    private final ApplicationEventPublisher eventPublisher;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    @Override
    public RecognitionSession handle(ActivateRecognitionCommand command) {
        log.info("Handling ActivateLprCommand for parking {}", command.parkingId());

        Optional<RecognitionSession> existingActive = sessionRepository.findActiveSessionByParkingId(command.parkingId(), RecognitionStatus.ACTIVE.toString(), LocalDateTime.now());

        if (existingActive.isPresent()) {
            log.warn("LPR already active for parking {}. Deactivating previous session.",
                    command.parkingId());
            existingActive.get().deactivate();
            sessionRepository.save(existingActive.get());
        }

        int timeout = command.timeoutSeconds() != null ? command.timeoutSeconds() : 30;
        RecognitionSession newSession = RecognitionSession.createActive(
                command.parkingId(),
                command.driverId(),
                timeout
        );

        RecognitionSession saved = sessionRepository.save(newSession);

        var event = RecognitionActivatedEvent.builder()
                .sessionId(saved.getId())
                .parkingId(saved.getParkingId())
                .driverId(saved.getDriverId())
                .activatedAt(saved.getActivatedAt())
                .timeoutAt(saved.getTimeoutAt())
                .timeoutSeconds(timeout)
                .build();

        eventPublisher.publishEvent(event);

        // Registrar en sincronización para cloud
        Map<String, Object> payload = new HashMap<>();
        payload.put("sessionId", saved.getId());
        payload.put("parkingId", saved.getParkingId());
        payload.put("driverId", saved.getDriverId());
        payload.put("activatedAt", saved.getActivatedAt());
        payload.put("timeoutAt", saved.getTimeoutAt());

        edgeSyncService.registerPendingSync(
                "LPR_ACTIVATED",
                saved.getId(),
                "LprSession",
                payload
        );

        log.info("LPR session activated: {} - Timeout in {} seconds", saved.getId(), timeout);
        return saved;
    }

    @Override
    public void handle(DeactivateRecognitionCommand command) {
        log.info("Handling DeactivateRecognitionCommand for session {}", command.sessionId());

        RecognitionSession session = sessionRepository.findById(command.sessionId()).orElseThrow(() -> new RuntimeException("Recognition Session not found: " + command.sessionId()));

        session.deactivate();
        sessionRepository.save(session);

        var event = RecognitionDeactivatedEvent.builder()
                .sessionId(session.getId())
                .parkingId(session.getParkingId())
                .driverId(session.getDriverId())
                .deactivatedAt(session.getDeactivatedAt())
                .timedOut(false)
                .reason("manual")
                .build();

        eventPublisher.publishEvent(event);

        // Registrar en sync
        Map<String, Object> payload = new HashMap<>();
        payload.put("sessionId", session.getId());
        payload.put("parkingId", session.getParkingId());
        payload.put("driverId", session.getDriverId());
        payload.put("deactivatedAt", session.getDeactivatedAt());
        payload.put("reason", "manual");

        edgeSyncService.registerPendingSync(
                "RECOGNITION_DEACTIVATED",
                session.getId(),
                "RecognitionSession",
                payload
        );

        log.info("Recognition session deactivated: {}", session.getId());
    }

    @Override
    public Optional<RecognitionSession> getActiveSession(UUID parkingId) {
        return sessionRepository.findActiveSessionByParkingId(parkingId, RecognitionStatus.ACTIVE.toString(), LocalDateTime.now());
    }

    @Override
    public Optional<RecognitionSession> getSessionById(UUID sessionId) {
        return sessionRepository.findById(sessionId);
    }

    @Override
    public Boolean handle(ActivateRecognition command) {
        log.info("🎯 Activating recognition process (quick capture)");

        var process = new RecognitionProcess();
        process.startProcess();
        processRepository.save(process);

        log.info("✅ Recognition process STARTED - isActive: true");

        scheduleAutoDeactivation(process.getId());

        return Boolean.TRUE;
    }

    @Override
    public Boolean handle(GetLatestStatusQuery command) {
        try {
            Optional<RecognitionProcess> latestProcess = processRepository.findLatestProcess();

            if (latestProcess.isPresent()) {
                Boolean isActive = latestProcess.get().getIsActive();
                log.debug("📊 Latest recognition process status: {}", isActive);
                return isActive;
            } else {
                log.debug("📊 No recognition process found");
                return false;
            }
        } catch (Exception e) {
            log.error("❌ Error getting process status: {}", e.getMessage(), e);
            return false;
        }
    }

    private void scheduleAutoDeactivation(UUID processId) {
        scheduler.schedule(() -> {
            try {
                log.info("⏰ Auto-deactivating recognition process: {}", processId);

                RecognitionProcess process = processRepository.findById(processId)
                        .orElse(null);

                if (process != null && process.getIsActive()) {
                    process.stopProcess();
                    processRepository.save(process);

                    log.info("✅ Recognition process STOPPED - isActive: false");
                }
            } catch (Exception e) {
                log.error("❌ Error during auto-deactivation: {}", e.getMessage(), e);
            }
        }, 10, TimeUnit.SECONDS);
    }
}
