package com.fortna.assignment.inventory_reservation.domain.repository;

import com.fortna.assignment.inventory_reservation.domain.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    boolean existsByOrderId(String orderId);

    @Query("SELECT DISTINCT r FROM Reservation r LEFT JOIN FETCH r.items i LEFT JOIN FETCH i.product WHERE r.id = :id")
    Optional<Reservation> findByIdWithItems(@Param("id") UUID id);
}
