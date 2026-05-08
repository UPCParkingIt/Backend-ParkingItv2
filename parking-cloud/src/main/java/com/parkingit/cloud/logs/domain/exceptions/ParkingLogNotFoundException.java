package com.parkingit.cloud.logs.domain.exceptions;

public class ParkingLogNotFoundException extends RuntimeException {
    public ParkingLogNotFoundException(String message) {
        super(message);
    }

    public ParkingLogNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
