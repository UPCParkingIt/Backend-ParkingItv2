package com.parkingit.cloud.parking.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Objects;

@Embeddable
@Getter
public class OperationSchedule {
    @Column(name = "operation_open_time", nullable = false)
    private LocalTime openTime;

    @Column(name = "operation_close_time", nullable = false)
    private LocalTime closeTime;

    @Column(name = "operation_business_days", nullable = false, length = 7)
    private String businessDays;

    protected OperationSchedule() {}

    public OperationSchedule(LocalTime openTime, LocalTime closeTime, String businessDays) {
        if (openTime == null || closeTime == null || businessDays == null) {
            throw new IllegalArgumentException("Schedule data cannot be null");
        }

        if (!openTime.isBefore(closeTime)) {
            throw new IllegalArgumentException("Opening time must be before closing time");
        }

        if (businessDays.length() != 7 || !businessDays.matches("[0-1]{7}")) {
            throw new IllegalArgumentException("Business days must be 7 digits of 0 or 1");
        }

        this.openTime = openTime;
        this.closeTime = closeTime;
        this.businessDays = businessDays;
    }

    public static OperationSchedule create(LocalTime openTime, LocalTime closeTime, String businessDays) {
        return new OperationSchedule(openTime, closeTime, businessDays);
    }

    public boolean isOpenNow() {
        ZonedDateTime now = ZonedDateTime.now();
        LocalTime currentTime = now.toLocalTime();
        int dayOfWeek = now.getDayOfWeek().getValue(); // 1=Monday, ..., 7=Sunday
        int dayIndex = dayOfWeek == 7 ? 6 : dayOfWeek - 1;

        if (businessDays.charAt(dayIndex) == '0') {
            return false;
        }

        return !currentTime.isBefore(openTime) && currentTime.isBefore(closeTime);
    }

    public boolean isOpenAt(ZonedDateTime dateTime) {
        LocalTime time = dateTime.toLocalTime();
        int dayOfWeek = dateTime.getDayOfWeek().getValue();
        int dayIndex = dayOfWeek == 7 ? 6 : dayOfWeek - 1;

        if (businessDays.charAt(dayIndex) == '0') {
            return false;
        }

        return !time.isBefore(openTime) && time.isBefore(closeTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OperationSchedule that = (OperationSchedule) o;
        return Objects.equals(openTime, that.openTime) &&
                Objects.equals(closeTime, that.closeTime) &&
                Objects.equals(businessDays, that.businessDays);
    }

    @Override
    public int hashCode() {
        return Objects.hash(openTime, closeTime, businessDays);
    }
}
