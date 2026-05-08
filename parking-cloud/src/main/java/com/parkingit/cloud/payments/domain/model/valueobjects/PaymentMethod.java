package com.parkingit.cloud.payments.domain.model.valueobjects;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    YAPE("Yape Billetera Digital", "PE", 0.0),           // 0% commission
    PLIN("Plin Billetera Digital", "PE", 0.0),           // 0% commission
    CULQI("Culqi Card Payment", "PE", 2.99),             // 2.99% + S/0.50
    CASH("Efectivo en Parking", "PE", 0.0),              // No commission
    BANK_TRANSFER("Transferencia Bancaria", "PE", 0.0),  // Manual
    WALLET("Internal Wallet", "PE", 0.0),                // Future
    MOCK("Mock Payment (Testing)", "PE", 0.0);           // Testing only

    private final String displayName;
    private final String country;
    private final double commissionPercentage;

    PaymentMethod(String displayName, String country, double commissionPercentage) {
        this.displayName = displayName;
        this.country = country;
        this.commissionPercentage = commissionPercentage;
    }
}
