package com.fortna.assignment.inventory_reservation.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inventory")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    @Id
    @Column(name = "sku")
    private String sku;

    @Column(nullable = false)
    private int totalStock;

    @Column(nullable = false)
    private int availableStock;

    @Column(nullable = false)
    private int reservedStock;

    public void reserve(int quantity) {
        this.availableStock -= quantity;
        this.reservedStock += quantity;
    }

    public void release(int quantity) {
        this.reservedStock -= quantity;
        this.availableStock += quantity;
    }
}
