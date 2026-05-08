package com.parkingit.cloud.logs.domain.services;

import com.parkingit.cloud.logs.domain.model.queries.*;
import com.parkingit.cloud.logs.domain.model.aggregates.ParkingLog;

import java.util.List;
import java.util.Optional;

public interface LogQueryService {
    Optional<ParkingLog> handle(GetParkingLogByIdQuery query);
    List<ParkingLog> handle(GetAllParkingLogsByParkingIdQuery query);
    List<ParkingLog> handle(GetAllParkingLogsByUserIdQuery query);
    List<ParkingLog> handle(GetAllAlertsGeneratedByParkingIdQuery query);
}
