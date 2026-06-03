package com.fortna.assignment.inventory_reservation.domain.exception;

import java.util.UUID;

public class ReservationNotFoundException extends DomainException {
    public ReservationNotFoundException(UUID id) {
        super("Reservation not found: " + id);
    }
}
