package com.fortna.assignment.inventory_reservation.domain.factory;

import com.fortna.assignment.inventory_reservation.domain.model.Reservation;
import com.fortna.assignment.inventory_reservation.domain.model.ReservationItem;
import com.fortna.assignment.inventory_reservation.domain.model.ReservationStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReservationFactory {

    public Reservation create(String orderId, List<ReservationItem> items) {
        Reservation reservation = Reservation.builder()
                .orderId(orderId)
                .status(ReservationStatus.PENDING)
                .build();

        items.forEach(item -> item.setReservation(reservation));
        reservation.setItems(items);

        return reservation;
    }
}
