package com.fortna.assignment.inventory_reservation.domain.model.state;

import com.fortna.assignment.inventory_reservation.domain.model.Reservation;
import com.fortna.assignment.inventory_reservation.domain.model.ReservationStatus;

public class PendingState implements ReservationState {

    public static final PendingState INSTANCE = new PendingState();
    private PendingState() {}

    @Override
    public void confirm(Reservation context) {
        context.setStatus(ReservationStatus.CONFIRMED);
    }

    @Override
    public void cancel(Reservation context) {
        context.setStatus(ReservationStatus.CANCELLED);
    }
}
