package com.fortna.assignment.inventory_reservation.domain.repository;

import com.fortna.assignment.inventory_reservation.domain.model.ReservationItem;
import com.fortna.assignment.inventory_reservation.domain.model.ReservationItemId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationItemRepository extends JpaRepository<ReservationItem, ReservationItemId> {
}
