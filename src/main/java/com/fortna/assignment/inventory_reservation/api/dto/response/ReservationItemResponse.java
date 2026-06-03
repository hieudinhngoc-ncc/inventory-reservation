package com.fortna.assignment.inventory_reservation.api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReservationItemResponse {
    private String sku;
    private int quantity;
}
