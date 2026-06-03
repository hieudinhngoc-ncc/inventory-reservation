package com.fortna.assignment.inventory_reservation.domain.model.state;

import com.fortna.assignment.inventory_reservation.domain.model.Reservation;
import com.fortna.assignment.inventory_reservation.domain.exception.InvalidStateTransitionException;

public class CancelledState implements ReservationState {

    public static final CancelledState INSTANCE = new CancelledState();
    private CancelledState() {}

    @Override
    public void confirm(Reservation context) {
        throw new InvalidStateTransitionException("A cancelled reservation cannot be confirmed");
    }

    @Override
    public void cancel(Reservation context) {
        throw new InvalidStateTransitionException("Reservation is already cancelled");
    }
}
