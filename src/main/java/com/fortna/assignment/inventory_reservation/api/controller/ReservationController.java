package com.fortna.assignment.inventory_reservation.api.controller;

import com.fortna.assignment.inventory_reservation.api.dto.request.CreateReservationRequest;
import com.fortna.assignment.inventory_reservation.api.dto.response.ApiResponse;
import com.fortna.assignment.inventory_reservation.api.dto.response.ReservationResponse;
import com.fortna.assignment.inventory_reservation.application.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReservationResponse> createReservation(
            @Valid @RequestBody CreateReservationRequest request) {
        return ApiResponse.success(reservationService.createReservation(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<ReservationResponse> getReservation(@PathVariable UUID id) {
        return ApiResponse.success(reservationService.getReservation(id));
    }

    @PostMapping("/{id}/confirm")
    public ApiResponse<ReservationResponse> confirmReservation(@PathVariable UUID id) {
        return ApiResponse.success(reservationService.confirmReservation(id));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<ReservationResponse> cancelReservation(@PathVariable UUID id) {
        return ApiResponse.success(reservationService.cancelReservation(id));
    }
}
