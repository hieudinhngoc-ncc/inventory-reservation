package com.fortna.assignment.inventory_reservation.domain.exception;

public class OrderAlreadyExistsException extends DomainException {
    public OrderAlreadyExistsException(String orderId) {
        super("A reservation already exists for order: " + orderId);
    }
}
