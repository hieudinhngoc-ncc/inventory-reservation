package com.fortna.assignment.inventory_reservation.domain.model.state;

import com.fortna.assignment.inventory_reservation.domain.exception.InvalidStateTransitionException;

public class ConfirmedState implements ReservationState {

    @Override
    public void confirm() {
        throw new InvalidStateTransitionException("Reservation is already confirmed");
    }

    @Override
    public void cancel() {
        throw new InvalidStateTransitionException("A confirmed reservation cannot be cancelled");
    }
}
