package com.fortna.assignment.inventory_reservation.api.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateReservationRequest {

    @NotBlank(message = "Order ID is required")
    private String orderId;

    @NotEmpty(message = "Reservation must contain at least one item")
    @Valid
    private List<ReservationItemRequest> items;
}
