package com.fortna.assignment.inventory_reservation.api.controller;

import com.fortna.assignment.inventory_reservation.api.dto.response.ApiResponse;
import com.fortna.assignment.inventory_reservation.api.dto.response.InventoryResponse;
import com.fortna.assignment.inventory_reservation.application.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{sku}")
    public ApiResponse<InventoryResponse> getInventory(@PathVariable String sku) {
        return ApiResponse.success(inventoryService.getInventory(sku));
    }
}
