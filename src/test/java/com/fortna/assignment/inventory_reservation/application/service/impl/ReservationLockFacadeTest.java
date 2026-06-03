package com.fortna.assignment.inventory_reservation.application.service.impl;

import com.fortna.assignment.inventory_reservation.application.command.CreateReservationCommand;
import com.fortna.assignment.inventory_reservation.application.command.ReservationItemCommand;
import com.fortna.assignment.inventory_reservation.domain.exception.SystemBusyException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationLockFacadeTest {

    @Mock
    private RedissonClient redissonClient;

    @Mock
    private ReservationTransactionService transactionService;

    @InjectMocks
    private ReservationLockFacade lockFacade;

    @Test
    void createReservation_whenLockNotAcquired_throwsSystemBusyException() throws InterruptedException {
        CreateReservationCommand command = CreateReservationCommand.builder()
                .orderId("ORD-1")
                .items(List.of(ReservationItemCommand.builder().sku("A100").quantity(1).build()))
                .build();

        RLock mockLock = mock(RLock.class);
        when(redissonClient.getLock("lock:inventory:A100")).thenReturn(mockLock);
        when(mockLock.tryLock(anyLong(), anyLong(), eq(TimeUnit.MILLISECONDS))).thenReturn(false);

        assertThatThrownBy(() -> lockFacade.createReservation(command))
                .isInstanceOf(SystemBusyException.class);
                
        verify(transactionService, never()).createReservation(any());
    }
}
