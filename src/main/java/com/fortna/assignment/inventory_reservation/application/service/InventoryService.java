package com.fortna.assignment.inventory_reservation.application.service;

import com.fortna.assignment.inventory_reservation.api.dto.response.InventoryResponse;

public interface InventoryService {
    InventoryResponse getInventory(String sku);
}
