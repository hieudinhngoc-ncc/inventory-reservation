package com.fortna.assignment.inventory_reservation.domain.model.state;

import com.fortna.assignment.inventory_reservation.domain.exception.InvalidStateTransitionException;

public class CancelledState implements ReservationState {

    @Override
    public void confirm() {
        throw new InvalidStateTransitionException("A cancelled reservation cannot be confirmed");
    }

    @Override
    public void cancel() {
        throw new InvalidStateTransitionException("Reservation is already cancelled");
    }
}
