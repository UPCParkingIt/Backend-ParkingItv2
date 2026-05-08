package com.parkingit.cloud.logs.application.internal.commandservices;

import com.parkingit.cloud.logs.domain.exceptions.InvalidLogException;
import com.parkingit.cloud.logs.domain.exceptions.ParkingLogNotFoundException;
import com.parkingit.cloud.logs.domain.model.aggregates.ParkingLog;
import com.parkingit.cloud.logs.domain.model.commands.RecordEntryLogCommand;
import com.parkingit.cloud.logs.domain.model.commands.RecordExitLogCommand;
import com.parkingit.cloud.logs.domain.model.entities.EntryLog;
import com.parkingit.cloud.logs.domain.model.entities.ExitLog;
import com.parkingit.cloud.logs.domain.model.events.AlertGeneratedFromLogEvent;
import com.parkingit.cloud.logs.domain.model.events.EntryLogRecordedEvent;
import com.parkingit.cloud.logs.domain.model.events.ExitLogRecordedEvent;
import com.parkingit.cloud.logs.domain.model.valueobjects.VerificationResult;
import com.parkingit.shared.domain.valueobjects.FacialEmbedding;
import com.parkingit.shared.domain.valueobjects.LicensePlate;
import com.parkingit.cloud.logs.domain.services.LogCommandService;
import com.parkingit.cloud.logs.infrastructure.persistence.jpa.repositories.EntryLogRepository;
import com.parkingit.cloud.logs.infrastructure.persistence.jpa.repositories.ExitLogRepository;
import com.parkingit.cloud.logs.infrastructure.persistence.jpa.repositories.LogRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class LogCommandServiceImpl implements LogCommandService {
    private final LogRepository logRepository;
    private final EntryLogRepository entryLogRepository;
    private final ExitLogRepository exitLogRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Optional<ParkingLog> handle(RecordEntryLogCommand command) {
        if (command.licensePlate() == null || command.licensePlate().isBlank()) {
            throw new InvalidLogException("License plate cannot be null or empty");
        }
        if (command.facialEmbedding() == null || command.facialEmbedding().isBlank()) {
            throw new InvalidLogException("Facial embedding cannot be null or empty");
        }
        if (command.parkingId() == null) {
            throw new InvalidLogException("Parking ID cannot be null");
        }

        LicensePlate licensePlate = new LicensePlate(command.licensePlate());
        FacialEmbedding facialEmbedding = new FacialEmbedding(command.facialEmbedding());

        EntryLog entryLog = EntryLog.create(licensePlate, facialEmbedding, command.parkingId(), command.userId());
        entryLogRepository.save(entryLog);

        ParkingLog parkingLog = ParkingLog.createFromEntry(licensePlate, entryLog, command.parkingId(), command.userId());
        parkingLog = logRepository.save(parkingLog);

        EntryLogRecordedEvent event = new EntryLogRecordedEvent(
                parkingLog.getId(),
                parkingLog.getParkingId(),
                parkingLog.getUserId(),
                licensePlate.getValue(),
                entryLog.getEntryTimestamp().toInstant(ZoneOffset.UTC)
        );
        eventPublisher.publishEvent(event);

        return Optional.of(parkingLog);
    }

    @Override
    public Optional<ParkingLog> handle(RecordExitLogCommand command) {
        if (command.entryLogId() == null) {
            throw new InvalidLogException("Entry log ID cannot be null");
        }
        if (command.licensePlate() == null || command.licensePlate().isBlank()) {
            throw new InvalidLogException("License plate cannot be null or empty");
        }
        if (command.facialEmbedding() == null || command.facialEmbedding().isBlank()) {
            throw new InvalidLogException("Facial embedding cannot be null or empty");
        }
        if (command.isMatched() == null || command.confidenceScore() == null) {
            throw new InvalidLogException("Verification result cannot be null");
        }

        ParkingLog parkingLog = logRepository.findAll().stream()
                .filter(pl -> pl.getEntryLog() != null && pl.getEntryLog().getId().equals(command.entryLogId()))
                .findFirst()
                .orElseThrow(() -> new ParkingLogNotFoundException("Parking log not found for entry log ID: " + command.entryLogId()));

        LicensePlate licensePlate = new LicensePlate(command.licensePlate());
        FacialEmbedding facialEmbedding = new FacialEmbedding(command.facialEmbedding());
        VerificationResult verificationResult = VerificationResult.create(command.isMatched(), command.confidenceScore());

        ExitLog exitLog = ExitLog.create(licensePlate, facialEmbedding, verificationResult, command.parkingId(), command.entryLogId());
        exitLog = exitLogRepository.save(exitLog);

        parkingLog.recordExit(exitLog);
        parkingLog = logRepository.save(parkingLog);

        ExitLogRecordedEvent event = new ExitLogRecordedEvent(
                parkingLog.getId(),
                parkingLog.getParkingId(),
                licensePlate.getValue(),
                command.isMatched(),
                parkingLog.getOccupancyDurationMinutes(),
                exitLog.getExitTimestamp().toInstant(ZoneOffset.UTC)
        );
        eventPublisher.publishEvent(event);

        if (parkingLog.getIsAlertGenerated()) {
            AlertGeneratedFromLogEvent alertEvent = new AlertGeneratedFromLogEvent(
                    parkingLog.getId(),
                    parkingLog.getParkingId(),
                    licensePlate.getValue(),
                    parkingLog.getAlertReason(),
                    LocalDateTime.now().toInstant(ZoneOffset.UTC)
            );
            eventPublisher.publishEvent(alertEvent);
        }

        return Optional.of(parkingLog);
    }
}
