package com.fortna.assignment.inventory_reservation.domain.exception;

public class InsufficientStockException extends DomainException {
    public InsufficientStockException(String sku, int available, int requested) {
        super(String.format(
                "SKU %s has only %d units available, %d were requested",
                sku, available, requested));
    }
}
