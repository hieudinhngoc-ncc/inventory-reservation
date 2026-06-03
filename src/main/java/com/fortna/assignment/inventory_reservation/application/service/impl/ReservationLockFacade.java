package com.fortna.assignment.inventory_reservation.application.service.impl;

import com.fortna.assignment.inventory_reservation.application.command.CreateReservationCommand;
import com.fortna.assignment.inventory_reservation.api.dto.response.ReservationResponse;
import com.fortna.assignment.inventory_reservation.application.service.ReservationService;
import com.fortna.assignment.inventory_reservation.domain.exception.SystemBusyException;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationLockFacade implements ReservationService {

    private static final long WAIT_TIME_MS  = 500;
    private static final long LEASE_TIME_MS = 10_000;

    private final RedissonClient redissonClient;
    private final ReservationTransactionService transactionService;

    @Override
    public ReservationResponse createReservation(CreateReservationCommand request) {
        List<String> sortedSkus = request.getItems().stream()
                .map(item -> item.getSku())
                .distinct()
                .sorted()
                .toList();

        List<RLock> locks = sortedSkus.stream()
                .map(sku -> redissonClient.getLock("lock:inventory:" + sku))
                .collect(Collectors.toList());

        try {
            for (RLock lock : locks) {
                boolean acquired = lock.tryLock(WAIT_TIME_MS, LEASE_TIME_MS, TimeUnit.MILLISECONDS);
                if (!acquired) {
                    throw new SystemBusyException(sortedSkus.get(locks.indexOf(lock)));
                }
            }
            return transactionService.createReservation(request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SystemBusyException(sortedSkus.get(0));
        } finally {
            // Reverse order to mirror the acquisition sequence
            for (int i = locks.size() - 1; i >= 0; i--) {
                RLock lock = locks.get(i);
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }

    @Override
    public ReservationResponse confirmReservation(UUID id) {
        return transactionService.confirmReservation(id);
    }

    @Override
    public ReservationResponse cancelReservation(UUID id) {
        RLock lock = redissonClient.getLock("lock:reservation:" + id);
        try {
            boolean acquired = lock.tryLock(WAIT_TIME_MS, LEASE_TIME_MS, TimeUnit.MILLISECONDS);
            if (!acquired) {
                throw new SystemBusyException(id.toString());
            }
            return transactionService.cancelReservation(id);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SystemBusyException(id.toString());
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public ReservationResponse getReservation(UUID id) {
        return transactionService.getReservation(id);
    }
}
