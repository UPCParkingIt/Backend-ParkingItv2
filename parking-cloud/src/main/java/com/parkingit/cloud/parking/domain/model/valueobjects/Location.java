package com.parkingit.cloud.parking.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.Objects;

@Embeddable
@Getter
public class Location {
    @Column(name = "location_latitude", nullable = false)
    private Double latitude;

    @Column(name = "location_longitude", nullable = false)
    private Double longitude;

    @Column(name = "location_address", nullable = false, length = 255)
    private String address;

    protected Location() {}

    public Location(Double latitude, Double longitude, String address) {
        if (latitude == null || longitude == null || address == null || address.isEmpty()) {
            throw new IllegalArgumentException("Location data cannot be empty");
        }

        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Invalid latitude: must be between -90 and 90");
        }

        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Invalid longitude: must be between -180 and 180");
        }

        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address.trim();
    }

    public static Location create(Double latitude, Double longitude, String address) {
        return new Location(latitude, longitude, address);
    }

    public double distanceTo(Location other) {
        final int EARTH_RADIUS_KM = 6371;

        double dLat = Math.toRadians(other.latitude - this.latitude);
        double dLon = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(other.latitude)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(latitude, location.latitude) &&
                Objects.equals(longitude, location.longitude) &&
                Objects.equals(address, location.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, address);
    }

    @Override
    public String toString() {
        return String.format("Location(%.6f, %.6f, %s)", latitude, longitude, address);
    }
}
