package com.parkingit.cloud.payments.interfaces.rest;

import com.parkingit.cloud.payments.domain.model.commands.ApprovePaymentCommand;
import com.parkingit.cloud.payments.domain.model.commands.MarkDriverPaidCommand;
import com.parkingit.cloud.payments.domain.model.commands.RejectPaymentCommand;
import com.parkingit.cloud.payments.domain.model.queries.GetAllPaymentsByReservationIdQuery;
import com.parkingit.cloud.payments.domain.model.queries.GetPaymentByIdQuery;
import com.parkingit.cloud.payments.domain.services.PaymentCommandService;
import com.parkingit.cloud.payments.domain.services.PaymentQueryService;
import com.parkingit.cloud.payments.interfaces.rest.resources.InitiatePaymentResource;
import com.parkingit.cloud.payments.interfaces.rest.resources.PaymentResource;
import com.parkingit.cloud.payments.interfaces.rest.transform.InitiatePaymentCommandFromResourceAssembler;
import com.parkingit.cloud.payments.interfaces.rest.transform.PaymentResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("LoggingSimilarMessage")
@RestController
@RequestMapping("/api/v1/payments")
@AllArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Payment processing and management endpoints")
public class PaymentsController {
    private final PaymentCommandService paymentCommandService;
    private final PaymentQueryService paymentQueryService;

    @PostMapping("")
    @Operation(summary = "Initiate a new payment", description = "Creates and initiates a payment for a reservation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Payment initiated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentResource.class))),
            @ApiResponse(responseCode = "400", description = "Invalid payment parameters"),
            @ApiResponse(responseCode = "409", description = "Payment method not available")
    })
    public ResponseEntity<PaymentResource> initiatePayment(@RequestBody InitiatePaymentResource request) {
        log.info("[PaymentsController] Initiating payment for reservation: {}", request.reservationId());

        try {
            var command = InitiatePaymentCommandFromResourceAssembler.toCommandFromResource(request);
            var payment = paymentCommandService.handle(command)
                    .orElseThrow(() -> new RuntimeException("Failed to initiate payment"));

            var response = PaymentResourceFromEntityAssembler.toResourceFromEntity(payment);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("[PaymentsController] Error initiating payment: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get payment by ID", description = "Retrieves payment details by its unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment found", content = @Content(mediaType = "application/json", schema = @Schema(implementation = PaymentResource.class))),
            @ApiResponse(responseCode = "404", description = "Payment not found")
    })
    public ResponseEntity<PaymentResource> getPaymentById(
            @Parameter(description = "Payment unique identifier") @PathVariable UUID id) {
        log.info("[PaymentsController] Fetching payment: id={}", id);

        try {
            var payment = paymentQueryService.handle(new GetPaymentByIdQuery(id))
                    .orElse(null);

            if (payment == null) {
                return ResponseEntity.notFound().build();
            }

            var response = PaymentResourceFromEntityAssembler.toResourceFromEntity(payment);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[PaymentsController] Error fetching payment: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/reservation/{reservationId}")
    @Operation(summary = "Get payments by reservation ID", description = "Retrieves all payments associated with a reservation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payments retrieved", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "404", description = "Reservation not found")
    })
    public ResponseEntity<List<PaymentResource>> getPaymentsByReservation(
            @Parameter(description = "Reservation unique identifier") @PathVariable UUID reservationId) {
        log.info("[PaymentsController] Fetching payments for reservation: {}", reservationId);

        try {
            var payments = paymentQueryService.handle(new GetAllPaymentsByReservationIdQuery(reservationId));
            var responses = payments.stream()
                    .map(PaymentResourceFromEntityAssembler::toResourceFromEntity)
                    .toList();

            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            log.error("[PaymentsController] Error fetching payments: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/available-methods")
    @Operation(summary = "Get available payment methods", description = "Lists all payment methods supported in Peru")
    @ApiResponse(responseCode = "200", description = "Available payment methods retrieved")
    public ResponseEntity<String> getAvailableMethods() {
        log.info("[PaymentsController] Fetching available payment methods");

        String methods = """
                Available Payment Methods:
                - YAPE: Billetera digital (sin comisión)
                - CASH: Pago en efectivo en parking
                """;

        return ResponseEntity.ok(methods);
    }

    @PostMapping("/{id}/mark-driver-paid")
    @Operation(summary = "Mark payment as driver paid", description = "Called when driver confirms payment in their Yape app")
    @ApiResponse(responseCode = "200", description = "Payment marked as driver paid - pending admin review")
    public ResponseEntity<PaymentResource> markDriverPaid(@PathVariable UUID id) {
        log.info("[PaymentsController] Marking payment as driver paid: {}", id);

        try {
            var command = new MarkDriverPaidCommand(id);
            var payment = paymentCommandService.handle(command)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            var response = PaymentResourceFromEntityAssembler.toResourceFromEntity(payment);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[PaymentsController] Error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve payment (Admin only)", description = "Admin approves or rejects payment")
    @ApiResponse(responseCode = "200", description = "Payment approved - driver can exit")
    public ResponseEntity<PaymentResource> approvePayment(
            @PathVariable UUID id,
            @RequestParam UUID adminId,
            @RequestParam String notes) {
        log.info("[PaymentsController] Approving payment: id={}, admin={}", id, adminId);

        try {
            var command = new ApprovePaymentCommand(id, adminId, notes);
            var payment = paymentCommandService.handle(command)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            var response = PaymentResourceFromEntityAssembler.toResourceFromEntity(payment);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[PaymentsController] Error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject payment (Admin only)", description = "Admin rejects payment - creates alert")
    @ApiResponse(responseCode = "200", description = "Payment rejected - alert created")
    public ResponseEntity<PaymentResource> rejectPayment(
            @PathVariable UUID id,
            @RequestParam UUID adminId,
            @RequestParam String reason) {
        log.info("[PaymentsController] Rejecting payment: id={}, admin={}", id, adminId);

        try {
            var command = new RejectPaymentCommand(id, adminId, reason);
            var payment = paymentCommandService.handle(command)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            var response = PaymentResourceFromEntityAssembler.toResourceFromEntity(payment);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[PaymentsController] Error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
