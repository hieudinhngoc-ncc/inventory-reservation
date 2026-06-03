package com.fortna.assignment.inventory_reservation.api.controller;

import com.fortna.assignment.inventory_reservation.api.dto.request.CreateReservationRequest;
import com.fortna.assignment.inventory_reservation.application.command.CreateReservationCommand;
import com.fortna.assignment.inventory_reservation.application.command.ReservationItemCommand;
import com.fortna.assignment.inventory_reservation.api.dto.response.ApiResponse;
import com.fortna.assignment.inventory_reservation.api.dto.response.ReservationResponse;
import com.fortna.assignment.inventory_reservation.application.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservation API", description = "Endpoints for managing inventory reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new reservation", description = "Reserves inventory for the given items")
    public ApiResponse<ReservationResponse> createReservation(
            @Valid @RequestBody CreateReservationRequest request) {
        CreateReservationCommand command = CreateReservationCommand.builder()
                .orderId(request.getOrderId())
                .items(request.getItems().stream()
                        .map(item -> ReservationItemCommand.builder()
                                .sku(item.getSku())
                                .quantity(item.getQuantity())
                                .build())
                        .toList())
                .build();
        return ApiResponse.success(reservationService.createReservation(command));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a reservation by ID", description = "Retrieves the details of an existing reservation")
    public ApiResponse<ReservationResponse> getReservation(@PathVariable UUID id) {
        return ApiResponse.success(reservationService.getReservation(id));
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm a reservation", description = "Moves the reservation status to CONFIRMED")
    public ApiResponse<ReservationResponse> confirmReservation(@PathVariable UUID id) {
        return ApiResponse.success(reservationService.confirmReservation(id));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a reservation", description = "Moves the reservation status to CANCELLED and releases inventory")
    public ApiResponse<ReservationResponse> cancelReservation(@PathVariable UUID id) {
        return ApiResponse.success(reservationService.cancelReservation(id));
    }
}
