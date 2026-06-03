package com.fortna.assignment.inventory_reservation.application.mapper;

import com.fortna.assignment.inventory_reservation.api.dto.response.ReservationResponse;
import com.fortna.assignment.inventory_reservation.domain.model.Product;
import com.fortna.assignment.inventory_reservation.domain.model.Reservation;
import com.fortna.assignment.inventory_reservation.domain.model.ReservationItem;
import com.fortna.assignment.inventory_reservation.domain.model.ReservationStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReservationMapperTest {

    private final ReservationMapper mapper = new ReservationMapper();

    @Test
    void toResponse_mapsFieldsCorrectly() {
        Product product = Product.builder().sku("A100").name("Test Product").build();
        ReservationItem item = ReservationItem.builder()
                .product(product)
                .quantity(5)
                .build();
        Reservation reservation = Reservation.builder()
                .id(UUID.randomUUID())
                .orderId("ORD-123")
                .status(ReservationStatus.PENDING)
                .items(List.of(item))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ReservationResponse response = mapper.toResponse(reservation);

        assertThat(response.getId()).isEqualTo(reservation.getId());
        assertThat(response.getOrderId()).isEqualTo("ORD-123");
        assertThat(response.getStatus()).isEqualTo(ReservationStatus.PENDING);
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getSku()).isEqualTo("A100");
        assertThat(response.getItems().get(0).getQuantity()).isEqualTo(5);
    }
}
