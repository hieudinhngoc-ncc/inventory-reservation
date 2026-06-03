package com.fortna.assignment.inventory_reservation.application.service.impl;

import com.fortna.assignment.inventory_reservation.application.command.CreateReservationCommand;
import com.fortna.assignment.inventory_reservation.application.command.ReservationItemCommand;
import com.fortna.assignment.inventory_reservation.api.dto.response.ReservationResponse;
import com.fortna.assignment.inventory_reservation.application.mapper.ReservationMapper;
import com.fortna.assignment.inventory_reservation.domain.exception.InsufficientStockException;
import com.fortna.assignment.inventory_reservation.domain.exception.InvalidStateTransitionException;
import com.fortna.assignment.inventory_reservation.domain.exception.OrderAlreadyExistsException;
import com.fortna.assignment.inventory_reservation.domain.exception.ReservationNotFoundException;
import com.fortna.assignment.inventory_reservation.domain.factory.ReservationFactory;
import com.fortna.assignment.inventory_reservation.domain.model.*;
import com.fortna.assignment.inventory_reservation.domain.repository.InventoryRepository;
import com.fortna.assignment.inventory_reservation.domain.repository.ProductRepository;
import com.fortna.assignment.inventory_reservation.domain.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationTransactionServiceTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private ProductRepository productRepository;
    @Mock private ReservationFactory reservationFactory;
    @Mock private ReservationMapper reservationMapper;

    @InjectMocks
    private ReservationTransactionService reservationService;

    private static final String ORDER_ID = "ORD-001";
    private static final String SKU = "A100";
    private static final UUID RESERVATION_ID = UUID.randomUUID();

    private Inventory inventory;
    private Reservation pendingReservation;
    private Reservation confirmedReservation;

    @BeforeEach
    void setUp() {
        inventory = Inventory.builder()
                .sku(SKU)
                .totalStock(100)
                .availableStock(100)
                .reservedStock(0)
                .build();

        pendingReservation = Reservation.builder()
                .id(RESERVATION_ID)
                .orderId(ORDER_ID)
                .status(ReservationStatus.PENDING)
                .items(List.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        confirmedReservation = Reservation.builder()
                .id(RESERVATION_ID)
                .orderId(ORDER_ID)
                .status(ReservationStatus.CONFIRMED)
                .items(List.of())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void createReservation_success_returnsPendingReservation() {
        CreateReservationCommand request = buildCommand(ORDER_ID, SKU, 10);

        when(reservationRepository.existsByOrderId(ORDER_ID)).thenReturn(false);
        when(inventoryRepository.findBySkuInWithLock(anyList())).thenReturn(List.of(inventory));
        when(productRepository.getReferenceById(SKU)).thenReturn(Product.builder().sku(SKU).name("Widget").build());
        when(reservationFactory.create(eq(ORDER_ID), anyList())).thenReturn(pendingReservation);
        when(reservationRepository.save(pendingReservation)).thenReturn(pendingReservation);
        when(reservationMapper.toResponse(pendingReservation)).thenReturn(mock(ReservationResponse.class));

        ReservationResponse response = reservationService.createReservation(request);

        assertThat(response).isNotNull();
        verify(inventoryRepository).findBySkuInWithLock(List.of(SKU));
        verify(reservationRepository).save(pendingReservation);
    }

    @Test
    void createReservation_insufficientStock_throwsInsufficientStockException() {
        inventory = Inventory.builder().sku(SKU).totalStock(5).availableStock(5).reservedStock(0).build();
        CreateReservationCommand request = buildCommand(ORDER_ID, SKU, 10);

        when(reservationRepository.existsByOrderId(ORDER_ID)).thenReturn(false);
        when(inventoryRepository.findBySkuInWithLock(anyList())).thenReturn(List.of(inventory));

        assertThatThrownBy(() -> reservationService.createReservation(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining(SKU)
                .hasMessageContaining("5")
                .hasMessageContaining("10");
    }

    @Test
    void createReservation_duplicateOrderId_throwsOrderAlreadyExistsException() {
        when(reservationRepository.existsByOrderId(ORDER_ID)).thenReturn(true);

        assertThatThrownBy(() -> reservationService.createReservation(buildCommand(ORDER_ID, SKU, 1)))
                .isInstanceOf(OrderAlreadyExistsException.class);
    }

    @Test
    void confirmReservation_fromPending_succeeds() {
        when(reservationRepository.findByIdWithItems(RESERVATION_ID)).thenReturn(Optional.of(pendingReservation));
        when(reservationRepository.save(any())).thenReturn(pendingReservation);
        when(reservationMapper.toResponse(any())).thenReturn(mock(ReservationResponse.class));

        reservationService.confirmReservation(RESERVATION_ID);

        assertThat(pendingReservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void confirmReservation_alreadyConfirmed_throwsInvalidStateTransition() {
        when(reservationRepository.findByIdWithItems(RESERVATION_ID)).thenReturn(Optional.of(confirmedReservation));

        assertThatThrownBy(() -> reservationService.confirmReservation(RESERVATION_ID))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void cancelReservation_fromPending_releasesStock() {
        Product product = Product.builder().sku(SKU).name("Widget").build();
        ReservationItem item = ReservationItem.builder()
                .product(product)
                .quantity(10)
                .build();
        Reservation reservation = Reservation.builder()
                .id(RESERVATION_ID)
                .orderId(ORDER_ID)
                .status(ReservationStatus.PENDING)
                .items(List.of(item))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        inventory = Inventory.builder().sku(SKU).totalStock(100).availableStock(90).reservedStock(10).build();

        when(reservationRepository.findByIdWithItems(RESERVATION_ID)).thenReturn(Optional.of(reservation));
        when(inventoryRepository.findBySkuInWithLock(anyList())).thenReturn(List.of(inventory));
        when(reservationRepository.save(any())).thenReturn(reservation);
        when(reservationMapper.toResponse(any())).thenReturn(mock(ReservationResponse.class));

        reservationService.cancelReservation(RESERVATION_ID);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(inventory.getAvailableStock()).isEqualTo(100);
        assertThat(inventory.getReservedStock()).isEqualTo(0);
    }

    @Test
    void cancelReservation_confirmedReservation_throwsInvalidStateTransition() {
        when(reservationRepository.findByIdWithItems(RESERVATION_ID)).thenReturn(Optional.of(confirmedReservation));

        assertThatThrownBy(() -> reservationService.cancelReservation(RESERVATION_ID))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("confirmed reservation cannot be cancelled");
    }

    @Test
    void getReservation_notFound_throwsReservationNotFoundException() {
        when(reservationRepository.findByIdWithItems(RESERVATION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.getReservation(RESERVATION_ID))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    private CreateReservationCommand buildCommand(String orderId, String sku, int quantity) {
        ReservationItemCommand item = ReservationItemCommand.builder()
                .sku(sku)
                .quantity(quantity)
                .build();

        return CreateReservationCommand.builder()
                .orderId(orderId)
                .items(List.of(item))
                .build();
    }
}
