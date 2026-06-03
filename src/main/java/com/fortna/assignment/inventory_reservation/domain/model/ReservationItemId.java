package com.fortna.assignment.inventory_reservation.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationItemId implements Serializable {
    // Field names must match the @Id field names declared in ReservationItem (JPA derived identity)
    private UUID reservation;
    private String product;
}
