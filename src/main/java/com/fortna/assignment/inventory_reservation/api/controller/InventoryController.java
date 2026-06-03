package com.fortna.assignment.inventory_reservation.api.controller;

import com.fortna.assignment.inventory_reservation.api.dto.response.ApiResponse;
import com.fortna.assignment.inventory_reservation.api.dto.response.InventoryResponse;
import com.fortna.assignment.inventory_reservation.application.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory API", description = "Endpoints for checking warehouse inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{sku}")
    @Operation(summary = "Get inventory stock", description = "Retrieves current available, reserved, and total stock for a given SKU")
    public ApiResponse<InventoryResponse> getInventory(@PathVariable String sku) {
        return ApiResponse.success(inventoryService.getInventory(sku));
    }
}
