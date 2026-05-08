package com.parkingit.cloud.logs.application.acl;

import com.parkingit.cloud.logs.domain.model.aggregates.ParkingLog;
import com.parkingit.cloud.logs.domain.model.commands.RecordEntryLogCommand;
import com.parkingit.cloud.logs.domain.model.commands.RecordExitLogCommand;
import com.parkingit.cloud.logs.domain.model.queries.GetAllAlertsGeneratedByParkingIdQuery;
import com.parkingit.cloud.logs.domain.model.queries.GetAllParkingLogsByParkingIdQuery;
import com.parkingit.cloud.logs.domain.model.valueobjects.OccupancyStats;
import com.parkingit.cloud.logs.domain.services.LogCommandService;
import com.parkingit.cloud.logs.domain.services.LogQueryService;
import com.parkingit.cloud.logs.interfaces.acl.LogContextFacade;
import com.parkingit.cloud.shared.domain.aggregates.AuditableAbstractAggregateRoot;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class LogContextFacadeImpl implements LogContextFacade {
    private final LogCommandService logCommandService;
    private final LogQueryService logQueryService;

    @Override
    public UUID recordEntry(String licensePlate, String facialEmbedding, UUID parkingId, UUID userId) {
        var command = new RecordEntryLogCommand(licensePlate, facialEmbedding, parkingId, userId);
        var result = logCommandService.handle(command);
        return result.map(AuditableAbstractAggregateRoot::getId).orElse(null);
    }

    @Override
    public UUID recordExit(UUID entryLogId, String licensePlate, String facialEmbedding, Boolean isMatched, BigDecimal confidenceScore, UUID parkingId) {
        var command = new RecordExitLogCommand(entryLogId, licensePlate, facialEmbedding, isMatched, confidenceScore, parkingId);
        var result = logCommandService.handle(command);
        return result.map(AuditableAbstractAggregateRoot::getId).orElse(null);
    }

    @Override
    public List<ParkingLog> fetchAllParkingLogsByParkingId(UUID parkingId) {
        var query = new GetAllParkingLogsByParkingIdQuery(parkingId);
        return logQueryService.handle(query);
    }

    @Override
    public List<ParkingLog> fetchAllAlertsGeneratedByParkingId(UUID parkingId) {
        var query = new GetAllAlertsGeneratedByParkingIdQuery(parkingId);
        return logQueryService.handle(query);
    }

    @Override
    public OccupancyStats fetchOccupancyStatsByParkingId(UUID parkingId) {
        var query = new GetAllParkingLogsByParkingIdQuery(parkingId);
        var logs = logQueryService.handle(query);

        long totalEntries = logs.size();
        long totalExits = logs.stream().filter(log -> log.getExitLog() != null).count();
        long matchedExits = logs.stream()
                .filter(log -> log.getStatus().toString().equals("MATCHED"))
                .count();
        long failedExits = totalExits - matchedExits;
        long alerts = logs.stream()
                .filter(log -> Boolean.TRUE.equals(log.getIsAlertGenerated()))
                .count();

        double averageOccupancy = logs.stream()
                .mapToLong(log -> log.getOccupancyDurationMinutes() != null ? log.getOccupancyDurationMinutes() : 0)
                .average()
                .orElse(0.0);

        double occupancyRate = totalEntries > 0 ? ((double) totalExits / totalEntries) * 100 : 0.0;

        return new OccupancyStats(
                totalEntries,
                totalExits,
                matchedExits,
                failedExits,
                alerts,
                averageOccupancy,
                occupancyRate
        );
    }
}
