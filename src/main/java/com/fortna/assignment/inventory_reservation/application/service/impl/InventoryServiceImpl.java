package com.fortna.assignment.inventory_reservation.application.service.impl;

import com.fortna.assignment.inventory_reservation.api.dto.response.InventoryResponse;
import com.fortna.assignment.inventory_reservation.application.mapper.InventoryMapper;
import com.fortna.assignment.inventory_reservation.application.service.InventoryService;
import com.fortna.assignment.inventory_reservation.domain.exception.SkuNotFoundException;
import com.fortna.assignment.inventory_reservation.domain.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventory(String sku) {
        return inventoryRepository.findById(sku)
                .map(inventoryMapper::toResponse)
                .orElseThrow(() -> new SkuNotFoundException(sku));
    }
}
