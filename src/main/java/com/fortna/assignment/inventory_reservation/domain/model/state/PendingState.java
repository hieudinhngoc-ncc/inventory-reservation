package com.fortna.assignment.inventory_reservation.domain.model.state;

public class PendingState implements ReservationState {

    @Override
    public void confirm() {
        // PENDING → CONFIRMED is a valid transition
    }

    @Override
    public void cancel() {
        // PENDING → CANCELLED is a valid transition
    }
}
