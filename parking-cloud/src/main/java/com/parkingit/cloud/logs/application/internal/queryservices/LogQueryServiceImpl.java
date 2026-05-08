package com.parkingit.cloud.logs.application.internal.queryservices;

import com.parkingit.cloud.logs.domain.model.aggregates.ParkingLog;
import com.parkingit.cloud.logs.domain.model.queries.GetAllAlertsGeneratedByParkingIdQuery;
import com.parkingit.cloud.logs.domain.model.queries.GetAllParkingLogsByParkingIdQuery;
import com.parkingit.cloud.logs.domain.model.queries.GetAllParkingLogsByUserIdQuery;
import com.parkingit.cloud.logs.domain.model.queries.GetParkingLogByIdQuery;
import com.parkingit.cloud.logs.domain.services.LogQueryService;
import com.parkingit.cloud.logs.infrastructure.persistence.jpa.repositories.EntryLogRepository;
import com.parkingit.cloud.logs.infrastructure.persistence.jpa.repositories.ExitLogRepository;
import com.parkingit.cloud.logs.infrastructure.persistence.jpa.repositories.LogRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class LogQueryServiceImpl implements LogQueryService {
    private final LogRepository logRepository;
    private final EntryLogRepository entryLogRepository;
    private final ExitLogRepository exitLogRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Optional<ParkingLog> handle(GetParkingLogByIdQuery query) {
        return logRepository.findById(query.id());
    }

    @Override
    public List<ParkingLog> handle(GetAllParkingLogsByParkingIdQuery query) {
        return logRepository.findAllByParkingId(query.parkingId());
    }

    @Override
    public List<ParkingLog> handle(GetAllParkingLogsByUserIdQuery query) {
        return logRepository.findAllByUserId(query.userId());
    }

    @Override
    public List<ParkingLog> handle(GetAllAlertsGeneratedByParkingIdQuery query) {
        return logRepository.findAllByParkingIdAndIsAlertGenerated(query.parkingId(), true);
    }
}
