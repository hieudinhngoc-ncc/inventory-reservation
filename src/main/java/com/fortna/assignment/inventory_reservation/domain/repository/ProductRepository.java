package com.fortna.assignment.inventory_reservation.domain.repository;

import com.fortna.assignment.inventory_reservation.domain.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String> {
}
