package com.fortna.assignment.inventory_reservation.application.service.impl;

import com.fortna.assignment.inventory_reservation.api.dto.request.CreateReservationRequest;
import com.fortna.assignment.inventory_reservation.api.dto.response.ReservationResponse;
import com.fortna.assignment.inventory_reservation.application.mapper.ReservationMapper;
import com.fortna.assignment.inventory_reservation.domain.exception.InsufficientStockException;
import com.fortna.assignment.inventory_reservation.domain.exception.OrderAlreadyExistsException;
import com.fortna.assignment.inventory_reservation.domain.exception.ReservationNotFoundException;
import com.fortna.assignment.inventory_reservation.domain.exception.SkuNotFoundException;
import com.fortna.assignment.inventory_reservation.domain.factory.ReservationFactory;
import com.fortna.assignment.inventory_reservation.domain.model.Inventory;
import com.fortna.assignment.inventory_reservation.domain.model.Reservation;
import com.fortna.assignment.inventory_reservation.domain.model.ReservationItem;
import com.fortna.assignment.inventory_reservation.domain.repository.InventoryRepository;
import com.fortna.assignment.inventory_reservation.domain.repository.ProductRepository;
import com.fortna.assignment.inventory_reservation.domain.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationTransactionService {

    private final ReservationRepository reservationRepository;
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final ReservationFactory reservationFactory;
    private final ReservationMapper reservationMapper;

    @Transactional
    public ReservationResponse createReservation(CreateReservationRequest request) {
        if (reservationRepository.existsByOrderId(request.getOrderId())) {
            throw new OrderAlreadyExistsException(request.getOrderId());
        }

        // Sorted alphabetically — consistent lock ordering prevents deadlock across concurrent transactions
        List<String> skus = request.getItems().stream()
                .map(item -> item.getSku())
                .distinct()
                .sorted()
                .toList();

        List<Inventory> inventories = inventoryRepository.findBySkuInWithLock(skus);

        if (inventories.size() != skus.size()) {
            Set<String> foundSkus = inventories.stream().map(Inventory::getSku).collect(Collectors.toSet());
            String missingSku = skus.stream().filter(s -> !foundSkus.contains(s)).findFirst().orElseThrow();
            throw new SkuNotFoundException(missingSku);
        }

        Map<String, Inventory> inventoryMap = inventories.stream()
                .collect(Collectors.toMap(Inventory::getSku, Function.identity()));

        // Validate all items before deducting anything — reject the entire request on first failure
        for (var itemReq : request.getItems()) {
            Inventory inventory = inventoryMap.get(itemReq.getSku());
            if (inventory.getAvailableStock() < itemReq.getQuantity()) {
                throw new InsufficientStockException(
                        itemReq.getSku(), inventory.getAvailableStock(), itemReq.getQuantity());
            }
        }

        request.getItems().forEach(itemReq ->
                inventoryMap.get(itemReq.getSku()).reserve(itemReq.getQuantity()));

        List<ReservationItem> items = request.getItems().stream()
                .map(itemReq -> ReservationItem.builder()
                        .product(productRepository.getReferenceById(itemReq.getSku()))
                        .quantity(itemReq.getQuantity())
                        .build())
                .toList();

        Reservation reservation = reservationFactory.create(request.getOrderId(), items);
        return reservationMapper.toResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse confirmReservation(UUID id) {
        Reservation reservation = findReservationWithItems(id);
        reservation.confirm();
        return reservationMapper.toResponse(reservationRepository.save(reservation));
    }

    @Transactional
    public ReservationResponse cancelReservation(UUID id) {
        Reservation reservation = findReservationWithItems(id);
        reservation.cancel();

        // Release stock back to inventory; sorted by SKU to prevent deadlock
        List<String> skus = reservation.getItems().stream()
                .map(item -> item.getProduct().getSku())
                .sorted()
                .toList();

        Map<String, Inventory> inventoryMap = inventoryRepository.findBySkuInWithLock(skus).stream()
                .collect(Collectors.toMap(Inventory::getSku, Function.identity()));

        reservation.getItems().forEach(item ->
                inventoryMap.get(item.getProduct().getSku()).release(item.getQuantity()));

        return reservationMapper.toResponse(reservationRepository.save(reservation));
    }

    @Transactional(readOnly = true)
    public ReservationResponse getReservation(UUID id) {
        return reservationMapper.toResponse(findReservationWithItems(id));
    }

    private Reservation findReservationWithItems(UUID id) {
        return reservationRepository.findByIdWithItems(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
    }
}
