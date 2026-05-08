package com.parkingit.cloud.reservations.domain.model.valueobjects;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Embeddable
@Getter
@NoArgsConstructor
public class ReservationTimeSlot {
    private LocalDateTime reservedFromTime;

    public ReservationTimeSlot(LocalDateTime reservedFromTime) {
        this.reservedFromTime = reservedFromTime;
    }

    public static ReservationTimeSlot create(LocalDateTime fromTime) {
        if (fromTime == null) {
            throw new IllegalArgumentException("Reservation arrival time cannot be null");
        }
        if (!fromTime.toLocalDate().equals(LocalDate.now())) {
            throw new IllegalArgumentException("Reservations can only be made for today");
        }
        if (fromTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reservation arrival time cannot be in the past");
        }
        return new ReservationTimeSlot(fromTime);
    }

    public boolean hasStarted() {
        return LocalDateTime.now().isAfter(reservedFromTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReservationTimeSlot that = (ReservationTimeSlot) o;
        return Objects.equals(reservedFromTime, that.reservedFromTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservedFromTime);
    }
}
