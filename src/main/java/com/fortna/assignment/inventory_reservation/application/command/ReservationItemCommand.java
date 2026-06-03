package com.fortna.assignment.inventory_reservation.application.command;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservationItemCommand {
    private String sku;
    private Integer quantity;
}
