package com.parkingit.cloud.parking.interfaces.rest.transform;

import com.parkingit.cloud.parking.domain.model.entities.Alert;
import com.parkingit.cloud.parking.domain.model.entities.ParkingPromotion;
import com.parkingit.cloud.parking.interfaces.rest.resources.PromotionResource;

public class PromotionResourceFromEntityAssembler {
    public static PromotionResource toResourceFromEntity(ParkingPromotion entity) {
        return new PromotionResource(
                entity.getId(),
                entity.getParkingId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getDiscountPercent(),
                entity.getValidFrom(),
                entity.getValidTo(),
                entity.isActive()
        );
    }
}
