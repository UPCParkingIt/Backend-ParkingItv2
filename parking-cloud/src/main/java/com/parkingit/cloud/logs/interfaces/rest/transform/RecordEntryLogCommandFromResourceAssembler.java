package com.parkingit.cloud.logs.interfaces.rest.transform;

import com.parkingit.cloud.logs.domain.model.commands.RecordEntryLogCommand;
import com.parkingit.cloud.logs.interfaces.rest.resources.CreateRecordEntryLogResource;

public class RecordEntryLogCommandFromResourceAssembler {
    public static RecordEntryLogCommand toCommandFromResource(CreateRecordEntryLogResource resource) {
        return new RecordEntryLogCommand(
                resource.licensePlate(),
                resource.facialEmbedding(),
                resource.parkingId(),
                resource.userId()
        );
    }
}
