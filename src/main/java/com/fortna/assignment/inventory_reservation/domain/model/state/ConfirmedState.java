package com.fortna.assignment.inventory_reservation.domain.model.state;

import com.fortna.assignment.inventory_reservation.domain.model.Reservation;
import com.fortna.assignment.inventory_reservation.domain.exception.InvalidStateTransitionException;

public class ConfirmedState implements ReservationState {

    public static final ConfirmedState INSTANCE = new ConfirmedState();
    private ConfirmedState() {}

    @Override
    public void confirm(Reservation context) {
        throw new InvalidStateTransitionException("Reservation is already confirmed");
    }

    @Override
    public void cancel(Reservation context) {
        throw new InvalidStateTransitionException("A confirmed reservation cannot be cancelled");
    }
}
