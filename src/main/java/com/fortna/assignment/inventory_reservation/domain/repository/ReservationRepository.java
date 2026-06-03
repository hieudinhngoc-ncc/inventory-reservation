package com.fortna.assignment.inventory_reservation.domain.repository;

import com.fortna.assignment.inventory_reservation.domain.model.Reservation;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository extends JpaRepository<Reservation, UUID> {

    boolean existsByOrderId(String orderId);

    @Query("SELECT DISTINCT r FROM Reservation r LEFT JOIN FETCH r.items i LEFT JOIN FETCH i.product WHERE r.id = :id")
    Optional<Reservation> findByIdWithItems(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    @Query("SELECT DISTINCT r FROM Reservation r LEFT JOIN FETCH r.items i LEFT JOIN FETCH i.product WHERE r.id = :id")
    Optional<Reservation> findByIdWithItemsForUpdate(@Param("id") UUID id);
}
