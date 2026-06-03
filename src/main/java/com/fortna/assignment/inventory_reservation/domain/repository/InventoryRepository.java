package com.fortna.assignment.inventory_reservation.domain.repository;

import com.fortna.assignment.inventory_reservation.domain.model.Inventory;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, String> {

    // ORDER BY sku ensures all concurrent transactions acquire row locks in the same alphabetical order,
    // preventing the circular-wait deadlock condition when multiple SKUs are reserved simultaneously.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    @Query("SELECT i FROM Inventory i WHERE i.sku IN :skus ORDER BY i.sku")
    List<Inventory> findBySkuInWithLock(@Param("skus") List<String> skus);
}
