package com.fortna.assignment.inventory_reservation.application.mapper;

import com.fortna.assignment.inventory_reservation.api.dto.response.InventoryResponse;
import com.fortna.assignment.inventory_reservation.domain.model.Inventory;
import org.springframework.stereotype.Component;

@Component
public class InventoryMapper {

    public InventoryResponse toResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .sku(inventory.getSku())
                .totalStock(inventory.getTotalStock())
                .availableStock(inventory.getAvailableStock())
                .reservedStock(inventory.getReservedStock())
                .build();
    }
}
