package com.parkingit.cloud.parking.interfaces.rest.resources;

public record OccupancyStatsResource(
        Long totalEntries,
        Long totalExits,
        Long matchedExits,
        Long failedExits,
        Long alerts,
        Double averageOccupancyMinutes,
        Double occupancyRate
) {
}
