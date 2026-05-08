package com.parkingit.cloud.logs.interfaces.acl;

import com.parkingit.cloud.logs.domain.model.aggregates.ParkingLog;
import com.parkingit.cloud.logs.domain.model.valueobjects.OccupancyStats;

import java.util.List;
import java.util.UUID;

public interface LogContextFacade {
    UUID recordEntry(String licensePlate, String facialEmbedding, UUID parkingId, UUID userId);
    UUID recordExit(UUID entryLogId, String licensePlate, String facialEmbedding, Boolean isMatched, java.math.BigDecimal confidenceScore, UUID parkingId);
    List<ParkingLog> fetchAllParkingLogsByParkingId(UUID parkingId);
    List<ParkingLog> fetchAllAlertsGeneratedByParkingId(UUID parkingId);
    OccupancyStats fetchOccupancyStatsByParkingId(UUID parkingId);
}
