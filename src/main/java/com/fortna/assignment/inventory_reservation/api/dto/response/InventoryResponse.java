package com.fortna.assignment.inventory_reservation.api.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventoryResponse {
    private String sku;
    private int totalStock;
    private int availableStock;
    private int reservedStock;
}
