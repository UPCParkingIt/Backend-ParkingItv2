package com.parkingit.cloud.iam.application.internal.eventhandlers;

import com.parkingit.cloud.iam.domain.model.commands.SeedRolesCommand;
import com.parkingit.cloud.iam.domain.services.RoleCommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

/**
 * ApplicationReadyEventHandler class
 * This class is used to handle the ApplicationReadyEvent
 */
@Service
@Slf4j
public class ApplicationReadyEventHandler {
    private final RoleCommandService roleCommandService;

    public ApplicationReadyEventHandler(RoleCommandService roleCommandService) {
        this.roleCommandService = roleCommandService;
    }

    /**
     * Handle the ApplicationReadyEvent
     * This method is used to seed the roles
     * @param event the ApplicationReadyEvent the event to handle
     */
    @EventListener
    public void on(ApplicationReadyEvent event) {
        var applicationName = event.getApplicationContext().getId();
        log.info("Starting to verify if roles seeding is needed for {} at {}",
            applicationName, currentTimestamp());

        var seedRolesCommand = new SeedRolesCommand();
        roleCommandService.handle(seedRolesCommand);
        log.info("Roles seeding verification finished for {} at {}",
            applicationName, currentTimestamp());
    }

    private Timestamp currentTimestamp() {
    return new Timestamp(System.currentTimeMillis());
  }
}
