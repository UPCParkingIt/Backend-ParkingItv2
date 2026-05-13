package com.parkingit.edge.recognition.application.internal.commandservices;

import com.parkingit.edge.recognition.infrastructure.persistence.jpa.repositories.ActiveVehicleRepository;
import com.parkingit.edge.recognition.domain.model.entities.ActiveVehicle;
import com.parkingit.edge.recognition.infrastructure.persistence.jpa.repositories.RecognitionProcessRepository;
import com.parkingit.edge.recognition.domain.model.entities.RecognitionProcess;
import com.parkingit.edge.sync.EdgeSyncService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecognitionEventAggregator {
    private final EdgeSyncService edgeSyncService;
    private final RecognitionProcessRepository processRepository;
    private final ActiveVehicleRepository activeVehicleRepository;

    // Buffer to hold events. Key is a group ID (we'll use a single parking entrance concept for MVP)
    // For MVP, we just hold the latest plate and latest face in a time window.
    private final Map<String, BufferedEvent> plateBuffer = new ConcurrentHashMap<>();
    private final Map<String, BufferedEvent> faceBuffer = new ConcurrentHashMap<>();

    private Map<String, Object> lastMatch = null;
    private LocalDateTime lastMatchTime = null;

    private static final int BUFFER_TIMEOUT_SECONDS = 60;
    
    // For MVP we will hardcode the parkingId or take it from the event if provided.
    // In a real scenario this comes from the Edge device configuration.
    private UUID currentParkingId = UUID.fromString("5def3b0b-5d35-423e-9922-3889501ae311");

    @Data
    private static class BufferedEvent {
        private String value;
        private LocalDateTime timestamp;
        private UUID parkingId;

        public BufferedEvent(String value, UUID parkingId) {
            this.value = value;
            this.timestamp = LocalDateTime.now();
            this.parkingId = parkingId;
        }

        public boolean isExpired() {
            return LocalDateTime.now().isAfter(timestamp.plusSeconds(BUFFER_TIMEOUT_SECONDS));
        }
    }

    public void addEvent(String type, String value, UUID parkingId) {
        log.info("=================================================");
        log.info("📢 [RECOGNITION] Evento recibido -> Tipo: {}, Valor: {}", type, value);
        log.info("=================================================");
        
        if (parkingId != null) {
            this.currentParkingId = parkingId;
        }

        if ("PLATE".equalsIgnoreCase(type)) {
            plateBuffer.put("GATE_1", new BufferedEvent(value, this.currentParkingId));
        } else if ("FACE".equalsIgnoreCase(type)) {
            faceBuffer.put("GATE_1", new BufferedEvent(value, this.currentParkingId));
        }

        tryToMatch();
    }

    private synchronized void tryToMatch() {
        BufferedEvent plateEvent = plateBuffer.get("GATE_1");
        BufferedEvent faceEvent = faceBuffer.get("GATE_1");

        if (plateEvent != null && faceEvent != null) {
            // Both events exist! Check if they are valid
            if (!plateEvent.isExpired() && !faceEvent.isExpired()) {
                log.info("⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐");
                log.info("✅ [RECOGNITION MATCH] Placa: {} | Rostro: {}", plateEvent.getValue(), faceEvent.getValue());
                log.info("⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐");

                UUID parkingId = plateEvent.getParkingId() != null ? plateEvent.getParkingId() : currentParkingId;
 
                // Determine if this is an ENTRY or EXIT event using ActiveVehicle table
                Optional<ActiveVehicle> activeVehicleOpt = activeVehicleRepository.findByLicensePlate(plateEvent.getValue());
                String mode;
                UUID entryLogId;
                
                if (activeVehicleOpt.isPresent()) {
                    // Car is inside, so it's an EXIT
                    mode = "EXIT";
                    entryLogId = activeVehicleOpt.get().getEntryLogId();
                    
                    // Remove from active vehicles
                    activeVehicleRepository.delete(activeVehicleOpt.get());
                    log.info("🚗 [AUTO-DETECT] Carro adentro, procesando como SALIDA. EntryLogId: {}", entryLogId);
                } else {
                    // Car is outside, so it's an ENTRY
                    mode = "ENTRY";
                    entryLogId = UUID.randomUUID(); // Generate new UUID for the entry

                    // Add to active vehicles
                    ActiveVehicle newActiveVehicle = new ActiveVehicle(plateEvent.getValue(), entryLogId, parkingId);
                    activeVehicleRepository.save(newActiveVehicle);
                    log.info("🚗 [AUTO-DETECT] Carro afuera, procesando como ENTRADA. Generado EntryLogId: {}", entryLogId);
                }
                
                String syncType = "EXIT".equals(mode) ? "VEHICLE_EXIT" : "VEHICLE_ENTRY";
                
                // Construct payload for Log Entry
                Map<String, Object> payload = new HashMap<>();
                payload.put("licensePlate", plateEvent.getValue());
                payload.put("facialEmbedding", faceEvent.getValue());
                payload.put("parkingId", parkingId);
                
                try {
                    payload.put("userId", UUID.fromString(faceEvent.getValue()));
                } catch (IllegalArgumentException e) {
                    payload.put("userId", UUID.randomUUID()); 
                }

                if ("EXIT".equals(mode)) {
                    boolean isMatched = faceEvent.getValue() != null
                            && !faceEvent.getValue().isBlank()
                            && !faceEvent.getValue().equalsIgnoreCase("INTRUSO");
                    double confidenceScore = isMatched ? 0.85 : 0.0;
                    payload.put("isMatched", isMatched);
                    payload.put("confidenceScore", confidenceScore);
                    payload.put("entryLogId", entryLogId);
                    log.info("🚨 [EXIT] isMatched={} (face={})", isMatched, faceEvent.getValue());
                } else {
                    payload.put("id", entryLogId); // Used as entryLogId by the cloud mapping
                }

                // Send to SyncService
                edgeSyncService.registerPendingSync(
                        syncType,
                        UUID.randomUUID(),
                        "EXIT".equals(mode) ? "ExitLog" : "EntryLog",
                        payload
                );

                // Save for polling
                this.lastMatch = new HashMap<>(payload);
                this.lastMatch.put("mode", mode);
                if ("EXIT".equals(mode)) {
                     this.lastMatch.put("entryLogId", entryLogId);
                }
                this.lastMatchTime = LocalDateTime.now();

                // Clear buffers after matching
                plateBuffer.remove("GATE_1");
                faceBuffer.remove("GATE_1");
            }
        }
    }

    public Map<String, Object> getLastMatch() {
        if (lastMatchTime != null && LocalDateTime.now().isBefore(lastMatchTime.plusSeconds(30))) {
            return lastMatch;
        }
        return null; // Expired after 30 seconds
    }

    public void clearLastMatch() {
        this.lastMatch = null;
        this.lastMatchTime = null;
    }

    public void registerManualEntry(String licensePlate) {
        Optional<ActiveVehicle> activeVehicleOpt = activeVehicleRepository.findByLicensePlate(licensePlate);
        if (activeVehicleOpt.isEmpty()) {
            ActiveVehicle newActiveVehicle = new ActiveVehicle(licensePlate, UUID.randomUUID(), currentParkingId);
            activeVehicleRepository.save(newActiveVehicle);
            log.info("🚗 [MANUAL-ENTRY] Vehículo registrado manualmente (por reserva). Placa: {}", licensePlate);
        }
    }

    public Map<String, Object> getLatestPlate() {
        BufferedEvent plateEvent = plateBuffer.get("GATE_1");
        if (plateEvent != null && !plateEvent.isExpired()) {
            boolean isExit = activeVehicleRepository.findByLicensePlate(plateEvent.getValue()).isPresent();
            Map<String, Object> response = new HashMap<>();
            response.put("licensePlate", plateEvent.getValue());
            response.put("isExit", isExit);
            return response;
        }
        return null;
    }

    @Scheduled(fixedRate = 5000)
    public void cleanupExpiredEvents() {
        plateBuffer.values().removeIf(BufferedEvent::isExpired);
        faceBuffer.values().removeIf(BufferedEvent::isExpired);
    }
}
