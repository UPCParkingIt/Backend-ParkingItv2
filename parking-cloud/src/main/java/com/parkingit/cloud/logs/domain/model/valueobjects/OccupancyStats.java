package com.parkingit.cloud.logs.domain.model.valueobjects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OccupancyStats {
    private Long totalEntries;
    private Long totalExits;
    private Long matchedExits;
    private Long failedExits;
    private Long alerts;
    private Double averageOccupancyMinutes;
    private Double occupancyRate;
}
