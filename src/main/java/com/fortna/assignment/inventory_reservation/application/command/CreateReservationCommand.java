package com.fortna.assignment.inventory_reservation.application.command;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CreateReservationCommand {
    private String orderId;
    private List<ReservationItemCommand> items;
}
