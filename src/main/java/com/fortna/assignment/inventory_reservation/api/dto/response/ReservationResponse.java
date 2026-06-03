package com.fortna.assignment.inventory_reservation.api.dto.response;

import com.fortna.assignment.inventory_reservation.domain.model.ReservationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ReservationResponse {
    private UUID id;
    private String orderId;
    private ReservationStatus status;
    private List<ReservationItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
