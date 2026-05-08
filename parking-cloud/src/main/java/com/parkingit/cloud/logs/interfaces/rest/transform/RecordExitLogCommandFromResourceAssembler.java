package com.parkingit.cloud.logs.interfaces.rest.transform;

import com.parkingit.cloud.logs.domain.model.commands.RecordExitLogCommand;
import com.parkingit.cloud.logs.interfaces.rest.resources.CreateRecordExitLogResource;

public class RecordExitLogCommandFromResourceAssembler {
    public static RecordExitLogCommand toCommandFromResource(CreateRecordExitLogResource resource) {
        return new RecordExitLogCommand(
                resource.entryLogId(),
                resource.licensePlate(),
                resource.facialEmbedding(),
                resource.isMatched(),
                resource.confidenceScore(),
                resource.parkingId()
        );
    }
}
