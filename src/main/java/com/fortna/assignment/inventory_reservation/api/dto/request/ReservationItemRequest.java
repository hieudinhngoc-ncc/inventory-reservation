package com.fortna.assignment.inventory_reservation.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReservationItemRequest {

    @NotBlank
    private String sku;

    @Min(1)
    private int quantity;
}
