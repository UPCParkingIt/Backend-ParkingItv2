package com.parkingit.cloud.logs.domain.services;

import com.parkingit.cloud.logs.domain.model.commands.RecordEntryLogCommand;
import com.parkingit.cloud.logs.domain.model.commands.RecordExitLogCommand;
import com.parkingit.cloud.logs.domain.model.aggregates.ParkingLog;

import java.util.Optional;

public interface LogCommandService {
    Optional<ParkingLog> handle(RecordEntryLogCommand command);
    Optional<ParkingLog> handle(RecordExitLogCommand command);
}
