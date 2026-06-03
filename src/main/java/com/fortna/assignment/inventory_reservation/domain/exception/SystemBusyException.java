package com.fortna.assignment.inventory_reservation.domain.exception;

public class SystemBusyException extends DomainException {

    public SystemBusyException(String sku) {
        super("System is busy processing another request for SKU: " + sku + ". Please retry shortly.");
    }
}
