package com.parkingit.cloud.parking.domain.exceptions;

public class ParkingNotFoundException extends RuntimeException {
    public ParkingNotFoundException(String message) {
        super(message);
    }

    public ParkingNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
