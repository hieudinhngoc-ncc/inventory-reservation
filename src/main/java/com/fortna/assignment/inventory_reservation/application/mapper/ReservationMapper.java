package com.fortna.assignment.inventory_reservation.application.mapper;

import com.fortna.assignment.inventory_reservation.api.dto.response.ReservationItemResponse;
import com.fortna.assignment.inventory_reservation.api.dto.response.ReservationResponse;
import com.fortna.assignment.inventory_reservation.domain.model.Reservation;
import com.fortna.assignment.inventory_reservation.domain.model.ReservationItem;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    public ReservationResponse toResponse(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .orderId(reservation.getOrderId())
                .status(reservation.getStatus())
                .items(reservation.getItems().stream().map(this::toItemResponse).toList())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }

    private ReservationItemResponse toItemResponse(ReservationItem item) {
        return ReservationItemResponse.builder()
                .sku(item.getProduct().getSku())
                .quantity(item.getQuantity())
                .build();
    }
}
