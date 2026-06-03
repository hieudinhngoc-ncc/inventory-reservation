package com.fortna.assignment.inventory_reservation.domain.model.state;

import com.fortna.assignment.inventory_reservation.domain.model.Reservation;

public interface ReservationState {
    void confirm(Reservation context);
    void cancel(Reservation context);
}
