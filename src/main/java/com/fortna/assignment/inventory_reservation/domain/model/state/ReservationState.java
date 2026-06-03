package com.fortna.assignment.inventory_reservation.domain.model.state;

public interface ReservationState {
    void confirm();
    void cancel();
}
