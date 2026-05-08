package com.parkingit.cloud.parking.domain.model.aggregates;

import com.parkingit.cloud.parking.domain.model.entities.Alert;
import com.parkingit.cloud.parking.domain.model.entities.ParkingPromotion;
import com.parkingit.cloud.parking.domain.model.entities.ParkingSpot;
import com.parkingit.cloud.parking.domain.model.valueobjects.*;
import com.parkingit.cloud.shared.domain.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.*;

@Entity
@Table(name = "parking_buildings")
@NoArgsConstructor
@Getter
@Setter
public class Parking extends AuditableAbstractAggregateRoot<Parking> {
    @Column(name = "parking_name", nullable = false, length = 255)
    private String parkingName;

    @Embedded
    private Location location;

    @Embedded
    private ParkingTariff tariff;

    @Embedded
    private OperationSchedule operationSchedule;

    @Column(name = "total_spots", nullable = false)
    private Integer totalSpots;

    @Column(name = "available_spots", nullable = false)
    private Integer availableSpots;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ParkingStatus status = ParkingStatus.OPEN;

    @Column(name = "admin_user_id", nullable = false)
    private UUID adminUserId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @OneToMany(
            mappedBy = "parking",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<ParkingSpot> parkingSpots = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_id")
    private Set<Alert> alerts = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "parking_id")
    private Set<ParkingPromotion> promotions = new HashSet<>();

    public static Parking create(
            String parkingName,
            Double latitude,
            Double longitude,
            String address,
            Integer totalSpots,
            BigDecimal baseTariffPerHour,
            String currency,
            LocalTime openTime,
            LocalTime closeTime,
            String businessDays,
            UUID adminUserId
    ) {
        if (parkingName == null || parkingName.isEmpty()) {
            throw new IllegalArgumentException("Parking name cannot be empty");
        }

        if (totalSpots == null || totalSpots <= 0) {
            throw new IllegalArgumentException("Total spots must be greater than 0");
        }

        if (adminUserId == null) {
            throw new IllegalArgumentException("Admin user ID cannot be null");
        }

        Parking parking = new Parking();
        parking.parkingName = parkingName.trim();
        parking.location = Location.create(latitude, longitude, address);
        parking.tariff = ParkingTariff.create(baseTariffPerHour, currency);
        parking.operationSchedule = OperationSchedule.create(openTime, closeTime, businessDays);
        parking.totalSpots = totalSpots;
        parking.availableSpots = totalSpots;
        parking.adminUserId = adminUserId;
        parking.status = ParkingStatus.OPEN;
        parking.isActive = true;

        for (int i = 1; i <= totalSpots; i++) {
            String section = String.valueOf((char) ('A' + (i - 1) / 20));
            String spotNumber = section + "-" + String.format("%02d", (i - 1) % 20 + 1);
            parking.parkingSpots.add(ParkingSpot.create(parking, spotNumber, section));
        }

        return parking;
    }

    public void updateTariff(BigDecimal newRate) {
        if (tariff == null) {
            throw new IllegalStateException("Parking tariff not initialized");
        }
        tariff.update(newRate);
    }

    public void updateSchedule(LocalTime openTime, LocalTime closeTime, String businessDays) {
        this.operationSchedule = OperationSchedule.create(openTime, closeTime, businessDays);
    }

    public boolean hasAvailableSpots() {
        return availableSpots > 0;
    }

    public double getOccupancyPercentage() {
        if (totalSpots == 0) return 0;
        return ((totalSpots - availableSpots) * 100.0) / totalSpots;
    }

    public void occupySpot(String licensePlate) {
        if (!hasAvailableSpots()) {
            status = ParkingStatus.FULL;
            throw new IllegalStateException("No available spots");
        }

        Optional<ParkingSpot> availableSpot = parkingSpots.stream()
                .filter(spot -> spot.getStatus() == OccupancyStatus.AVAILABLE)
                .findFirst();

        if (availableSpot.isPresent()) {
            availableSpot.get().occupy(licensePlate);
            availableSpots--;

            if (availableSpots == 0) {
                status = ParkingStatus.FULL;
            }
        }
    }

    public void releaseSpot(String licensePlate) {
        Optional<ParkingSpot> occupiedSpot = parkingSpots.stream()
                .filter(spot -> licensePlate.equals(spot.getOccupiedByVehiclePlate()))
                .findFirst();

        if (occupiedSpot.isPresent()) {
            occupiedSpot.get().release();
            availableSpots++;

            if (status == ParkingStatus.FULL && availableSpots > 0) {
                status = operationSchedule.isOpenNow() ? ParkingStatus.OPEN : ParkingStatus.CLOSED;
            }
        }
    }

    public void addAlert(AlertType type, AlertSeverity severity, String description, UUID parkingLogId) {
        Alert alert = Alert.create(getId(), type, severity, description);
        alert.setParkingLogId(parkingLogId);
        alerts.add(alert);
    }

    public void addPromotion(ParkingPromotion promotion) {
        if (promotion == null) {
            throw new IllegalArgumentException("Promotion cannot be null");
        }
        promotions.add(promotion);
    }

    public Set<ParkingPromotion> getActivePromotions() {
        Set<ParkingPromotion> activePromos = new HashSet<>();
        for (ParkingPromotion promo : promotions) {
            if (promo.isActive()) {
                activePromos.add(promo);
            }
        }
        return activePromos;
    }

    public void deactivate() {
        this.isActive = false;
        this.status = ParkingStatus.CLOSED;
    }

    public void activate() {
        this.isActive = true;
        this.status = operationSchedule.isOpenNow() ? ParkingStatus.OPEN : ParkingStatus.CLOSED;
    }

    public void validateForOperation() {
        if (!isActive) {
            throw new IllegalStateException("Parking is not active");
        }
        if (operationSchedule != null && !operationSchedule.isOpenNow()) {
            throw new IllegalStateException("Parking is closed at this time");
        }
    }
}
