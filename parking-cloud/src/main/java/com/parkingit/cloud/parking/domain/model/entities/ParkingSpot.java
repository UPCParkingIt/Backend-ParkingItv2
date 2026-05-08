package com.parkingit.cloud.parking.domain.model.entities;

import com.parkingit.cloud.parking.domain.model.aggregates.Parking;
import com.parkingit.cloud.parking.domain.model.valueobjects.OccupancyStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ParkingSpot {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_id", nullable = false)
    private Parking parking;

    @Column(name = "spot_number", nullable = false, length = 20)
    private String spotNumber;

    @Column(name = "location_section", nullable = false, length = 20)
    private String locationSection;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OccupancyStatus status = OccupancyStatus.AVAILABLE;

    @Column(name = "occupied_by")
    private String occupiedByVehiclePlate;

    @Column(name = "occupied_since")
    private LocalDateTime occupiedSince;

    public static ParkingSpot create(Parking parking, String spotNumber, String section) {
        ParkingSpot spot = new ParkingSpot();
        spot.parking = parking;
        spot.spotNumber = spotNumber;
        spot.locationSection = section;
        spot.status = OccupancyStatus.AVAILABLE;
        return spot;
    }

    public void occupy(String licensePlate) {
        this.status = OccupancyStatus.OCCUPIED;
        this.occupiedByVehiclePlate = licensePlate;
        this.occupiedSince = LocalDateTime.now();
    }

    public void release() {
        this.status = OccupancyStatus.AVAILABLE;
        this.occupiedByVehiclePlate = null;
        this.occupiedSince = null;
    }

    public void block() {
        this.status = OccupancyStatus.BLOCKED;
        this.occupiedByVehiclePlate = null;
        this.occupiedSince = null;
    }

    public int getOccupancyDurationMinutes() {
        if (status != OccupancyStatus.OCCUPIED || occupiedSince == null) {
            return 0;
        }
        return (int) ((System.currentTimeMillis() - occupiedSince.toLocalDate().toEpochDay() * 86400000) / 60000);
    }
}
