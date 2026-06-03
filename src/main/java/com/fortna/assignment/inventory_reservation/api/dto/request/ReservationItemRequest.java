package com.fortna.assignment.inventory_reservation.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReservationItemRequest {

    @NotBlank(message = "SKU is required")
    private String sku;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;
}
