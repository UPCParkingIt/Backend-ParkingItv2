package com.parkingit.cloud.parking.application.internal.commandservices;

import com.parkingit.cloud.notifications.application.internal.outboundservices.acl.ExternalIamService;
import com.parkingit.cloud.parking.domain.model.aggregates.Parking;
import com.parkingit.cloud.parking.domain.model.commands.*;
import com.parkingit.cloud.parking.domain.model.entities.Alert;
import com.parkingit.cloud.parking.domain.model.entities.ParkingPromotion;
import com.parkingit.cloud.parking.domain.model.events.AlertGeneratedEvent;
import com.parkingit.cloud.parking.domain.model.events.PromotionCreatedEvent;
import com.parkingit.cloud.parking.domain.model.valueobjects.AlertStatus;
import com.parkingit.cloud.parking.domain.model.valueobjects.Location;
import com.parkingit.cloud.parking.domain.services.ParkingCommandService;
import com.parkingit.cloud.parking.infrastructure.persistence.jpa.repositories.AlertRepository;
import com.parkingit.cloud.parking.infrastructure.persistence.jpa.repositories.ParkingRepository;
import com.parkingit.cloud.parking.infrastructure.persistence.jpa.repositories.PromotionRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
@Slf4j
public class ParkingCommandServiceImpl implements ParkingCommandService {
    private final ParkingRepository parkingRepository;
    private final AlertRepository alertRepository;
    private final PromotionRepository promotionRepository;
    private final ExternalIamService externalIamService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Optional<Parking> handle(CreateParkingCommand command) {
        try {
            var existingAdminUser = externalIamService.fetchUserById(command.adminUserId()).orElseThrow(() -> new IllegalArgumentException("Admin user with id " + command.adminUserId() + " does not exist."));

            if (parkingRepository.existsByAdminUserIdAndIsActiveTrue(command.adminUserId(), true)) {
                log.warn("[ParkingCommandService] Admin {} already has an active parking", command.adminUserId());
                throw new IllegalArgumentException("Admin already manages a parking");
            }

            Parking newParking = Parking.create(
                    command.parkingName(),
                    command.latitude(),
                    command.longitude(),
                    command.address(),
                    command.totalSpots(),
                    command.baseRatePerHour(),
                    command.currency(),
                    command.openTime(),
                    command.closeTime(),
                    command.businessDays(),
                    command.adminUserId(),
                    command.reservationFee()
            );

            var savedParking = parkingRepository.save(newParking);

            //eventPublisher.publishEvent(new ParkingCreatedEvent(savedParking.getId(), savedParking.getParkingName(), savedParking.getAdminUserId()));

            log.info("[ParkingCommandService] Parking created: {} (ID: {})", savedParking.getParkingName(), savedParking.getId());
            return Optional.of(savedParking);
        } catch (Exception e) {
            log.error("[ParkingCommandService] Error creating parking: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Error while creating parking: " + e.getMessage());
        }
    }

    @Override
    public void handle(DeactivateParkingCommand command) {
        try {
            var optParking = parkingRepository.findById(command.id());

            if (optParking.isEmpty()) {
                throw new IllegalArgumentException("Parking with id " + command.id() + " does not exist.");
            } else {
                var parking = optParking.get();

                parking.setIsActive(false);

                parkingRepository.save(parking);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while deleting parking: " + e.getMessage());
        }
    }

    @Override
    public Optional<Parking> handle(UpdateParkingCommand command) {
        try {
            var optParking = parkingRepository.findById(command.id());

            if (optParking.isEmpty()) {
                throw new IllegalArgumentException("Parking with id " + command.id() + " does not exist.");
            }

            var parking = optParking.get();

            if (command.parkingName() != null && !command.parkingName().isBlank()) {
                if (!command.parkingName().equals(parking.getParkingName())) {
                    parking.setParkingName(command.parkingName().trim());
                    log.debug("Parking name updated to: {}", command.parkingName());
                }
            }

            if (command.latitude() != null && command.longitude() != null && command.address() != null) {
                try {
                    Location newLocation = Location.create(command.latitude(), command.longitude(), command.address());
                    if (!newLocation.equals(parking.getLocation())) {
                        parking.setLocation(newLocation);
                        log.debug("[ParkingCommandService] Parking location updated to: ({}, {})", command.latitude(), command.longitude());
                    }
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid location data: " + e.getMessage());
                }
            }

            if (command.openTime() != null && command.closeTime() != null && command.businessDays() != null) {
                try {
                    parking.updateSchedule(command.openTime(), command.closeTime(), command.businessDays());
                    log.debug("[ParkingCommandService] Parking schedule updated: {} - {}", command.openTime(), command.closeTime());
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid schedule data: " + e.getMessage());
                }
            }

            if (command.newTariff() != null) {
                try {
                    parking.updateTariff(command.newTariff());
                    log.debug("[ParkingCommandService] Parking tariff updated to: {}", command.newTariff());
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid tariff data: " + e.getMessage());
                }
            }

            if (command.newReservationFee() != null) {
                try {
                    parking.setReservationFee(command.newReservationFee());
                    log.debug("[ParkingCommandService] Parking reservation fee updated to: {}", command.newReservationFee());
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Invalid reservation fee data: " + e.getMessage());
                }
            }

            var updatedParking = parkingRepository.save(parking);
            log.info("[ParkingCommandService] Parking updated successfully: {}", command.id());
            return Optional.of(updatedParking);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while updating parking: " + e.getMessage());
        }
    }

    @Override
    public void handle(ConfigureScheduleCommand command) {
        try {
            log.debug("[ParkingCommandService] Configuring schedule for parking: {}", command.parkingId());

            Parking parking = parkingRepository.findById(command.parkingId())
                    .orElseThrow(() -> new IllegalArgumentException("Parking not found"));

            parking.updateSchedule(command.openTime(), command.closeTime(), command.businessDays());
            parkingRepository.save(parking);

            log.info("[ParkingCommandService] Parking schedule configured: {}", command.parkingId());

        } catch (Exception e) {
            log.error("[ParkingCommandService] Error configuring schedule: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to configure schedule", e);
        }
    }

    @Override
    public Optional<Alert> handle(CreateAlertCommand command) {
        try {
            Parking parking = parkingRepository.findById(command.parkingId()).orElseThrow(() -> new IllegalArgumentException("Parking not found"));

            var newAlert = Alert.create(
                    command.parkingId(),
                    command.alertType(),
                    command.severity(),
                    command.description()
            );

            var savedAlert = alertRepository.save(newAlert);

            parking.addAlert(command.alertType(), command.severity(), command.description(), command.parkingLogId());

            parkingRepository.save(parking);

            eventPublisher.publishEvent(new AlertGeneratedEvent(UUID.randomUUID(), command.parkingId(), command.alertType(), command.severity(), command.description()));

            log.info("[ParkingCommandService] Alert created for parking: {} (Type: {}, Severity: {})", command.parkingId(), command.alertType(), command.severity());
            return Optional.of(savedAlert);
        } catch (Exception e) {
            log.error("[ParkingCommandService] Error creating alert: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create alert", e);
        }
    }

    @Override
    public void handle(ReviewAlertCommand command) {
        try {
            var optAlert = alertRepository.findById(command.alertId()).orElseThrow(() -> new IllegalArgumentException("Alert not found"));

            if (optAlert.getStatus() == AlertStatus.REVIEWED) {
                throw  new IllegalArgumentException("Alert is already reviewed");
            } else if (optAlert.getStatus() == AlertStatus.RESOLVED) {
                throw new IllegalArgumentException("Alert is already resolved");
            } else if (optAlert.getStatus() == AlertStatus.FALSE_ALARM) {
                throw new IllegalArgumentException("Alert is marked as false alarm");
            } else {
                optAlert.review(command.reviewerNotes());
                alertRepository.save(optAlert);
            }
        } catch (Exception e) {
            log.error("[ParkingCommandService] Error reviewing alert: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to review alert", e);
        }
    }

    @Override
    public void handle(ResolveAlertCommand command) {
        try {
            var optAlert = alertRepository.findById(command.alertId()).orElseThrow(() -> new IllegalArgumentException("Alert not found"));

            if (optAlert.getStatus() == AlertStatus.REVIEWED) {
                throw  new IllegalArgumentException("Alert is already reviewed");
            } else if (optAlert.getStatus() == AlertStatus.RESOLVED) {
                throw new IllegalArgumentException("Alert is already resolved");
            } else if (optAlert.getStatus() == AlertStatus.FALSE_ALARM) {
                throw new IllegalArgumentException("Alert is marked as false alarm");
            } else {
                optAlert.resolve(command.reviewerNotes());
                alertRepository.save(optAlert);
            }
        } catch (Exception e) {
            log.error("[ParkingCommandService] Error resolving alert: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to resolve alert", e);

        }
    }

    @Override
    public void handle(MarkAsFalseAlarmCommand command) {
        try {
            var optAlert = alertRepository.findById(command.alertId()).orElseThrow(() -> new IllegalArgumentException("Alert not found"));

            if (optAlert.getStatus() == AlertStatus.REVIEWED) {
                throw  new IllegalArgumentException("Alert is already reviewed");
            } else if (optAlert.getStatus() == AlertStatus.RESOLVED) {
                throw new IllegalArgumentException("Alert is already resolved");
            } else if (optAlert.getStatus() == AlertStatus.FALSE_ALARM) {
                throw new IllegalArgumentException("Alert is marked as false alarm");
            } else {
                optAlert.markAsFalseAlarm(command.reviewerNotes());
                alertRepository.save(optAlert);
            }
        } catch (Exception e) {
            log.error("[ParkingCommandService] Error marking alert as false alarm: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to mark alert as false alarm", e);
        }
    }

    @Override
    public Optional<ParkingPromotion> handle(CreatePromotionCommand command) {
        try {
            log.debug("[ParkingCommandService] Creating promotion for parking: {}", command.parkingId());

            Parking parking = parkingRepository.findById(command.parkingId()).orElseThrow(() -> new IllegalArgumentException("Parking not found"));

            var promotion = ParkingPromotion.create(
                    command.parkingId(),
                    command.title(),
                    command.description(),
                    command.discountPercent(),
                    command.validFrom(),
                    command.validTo()
            );

            var savedPromotion = promotionRepository.save(promotion);

            parking.addPromotion(promotion);

            parkingRepository.save(parking);

            eventPublisher.publishEvent(new PromotionCreatedEvent(promotion.getId(), command.parkingId(), command.title()));

            log.info("[ParkingCommandService] Promotion created: {} for parking {}", promotion.getId(), command.parkingId());
            return Optional.of(savedPromotion);
        } catch (Exception e) {
            log.error("[ParkingCommandService] Error creating promotion: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create promotion", e);
        }
    }

    @Override
    public void handle(DeactivatePromotionCommand command) {
        try {
            var optPromotion = promotionRepository.findById(command.id());

            if (optPromotion.isEmpty()) {
                throw new IllegalArgumentException("Promotion with id " + command.id() + " does not exist.");
            } else {
                var promotion = optPromotion.get();

                promotion.setIsActive(false);

                promotionRepository.save(promotion);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while deleting parking: " + e.getMessage());
        }
    }

    @Override
    public void handle(ActivatePromotionCommand command) {
        try {
            var optPromotion = promotionRepository.findById(command.id());

            if (optPromotion.isEmpty()) {
                throw new IllegalArgumentException("Promotion with id " + command.id() + " does not exist.");
            } else {
                var promotion = optPromotion.get();

                promotion.setIsActive(true);

                promotionRepository.save(promotion);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Error while activating promotion: " + e.getMessage());
        }
    }

    @Override
    public void handle(OccupySpotCommand command) {
        try {
            log.debug("[ParkingCommandService] Occupying spot for parking: {}", command.parkingId());

            Parking parking = parkingRepository.findById(command.parkingId())
                    .orElseThrow(() -> new IllegalArgumentException("Parking not found"));

            parking.occupySpot(command.licensePlate());
            parkingRepository.save(parking);

            log.info("[ParkingCommandService] Spot occupied in parking {} for vehicle {}", command.parkingId(), command.licensePlate());

        } catch (Exception e) {
            log.error("[ParkingCommandService] Error occupying spot: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to occupy spot", e);
        }
    }

    @Override
    public void handle(ReleaseSpotCommand command) {
        try {
            log.debug("[ParkingCommandService] Releasing spot for parking: {}", command.parkingId());

            Parking parking = parkingRepository.findById(command.parkingId())
                    .orElseThrow(() -> new IllegalArgumentException("Parking not found"));

            parking.releaseSpot(command.licensePlate());
            parkingRepository.save(parking);

            log.info("[ParkingCommandService] Spot released in parking {} for vehicle {}", command.parkingId(), command.licensePlate());

        } catch (Exception e) {
            log.error("[ParkingCommandService] Error releasing spot: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to release spot", e);
        }
    }
}
