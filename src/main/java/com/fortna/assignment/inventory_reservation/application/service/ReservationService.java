package com.fortna.assignment.inventory_reservation.application.service;

import com.fortna.assignment.inventory_reservation.api.dto.request.CreateReservationRequest;
import com.fortna.assignment.inventory_reservation.api.dto.response.ReservationResponse;

import java.util.UUID;

public interface ReservationService {
    ReservationResponse createReservation(CreateReservationRequest request);
    ReservationResponse confirmReservation(UUID id);
    ReservationResponse cancelReservation(UUID id);
    ReservationResponse getReservation(UUID id);
}
