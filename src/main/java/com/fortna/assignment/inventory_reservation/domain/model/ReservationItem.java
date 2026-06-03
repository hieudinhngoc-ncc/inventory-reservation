package com.fortna.assignment.inventory_reservation.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reservation_items")
@IdClass(ReservationItemId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationItem {

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sku", nullable = false)
    private Product product;

    @Column(nullable = false)
    private int quantity;
}
