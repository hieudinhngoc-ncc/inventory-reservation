package com.fortna.assignment.inventory_reservation.domain.exception;

public class SkuNotFoundException extends DomainException {
    public SkuNotFoundException(String sku) {
        super("SKU not found: " + sku);
    }
}
