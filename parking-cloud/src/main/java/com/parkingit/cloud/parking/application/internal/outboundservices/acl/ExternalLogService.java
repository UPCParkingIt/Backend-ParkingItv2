package com.parkingit.cloud.parking.application.internal.outboundservices.acl;

import com.parkingit.cloud.logs.domain.model.valueobjects.OccupancyStats;
import com.parkingit.cloud.logs.interfaces.acl.LogContextFacade;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class ExternalLogService {
    private final LogContextFacade logContextFacade;

    public OccupancyStats fetchOccupancyStatsByParkingId(UUID parkingId) {
        return logContextFacade.fetchOccupancyStatsByParkingId(parkingId);
    }
}
