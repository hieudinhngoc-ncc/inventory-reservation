package com.fortna.assignment.inventory_reservation.api.exception;

import com.fortna.assignment.inventory_reservation.api.dto.response.ApiResponse;
import com.fortna.assignment.inventory_reservation.domain.exception.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InsufficientStockException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleInsufficientStock(InsufficientStockException ex) {
        return ApiResponse.error("INSUFFICIENT_STOCK", ex.getMessage());
    }

    @ExceptionHandler(InvalidStateTransitionException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleInvalidStateTransition(InvalidStateTransitionException ex) {
        return ApiResponse.error("INVALID_STATE_TRANSITION", ex.getMessage());
    }

    @ExceptionHandler(ReservationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleReservationNotFound(ReservationNotFoundException ex) {
        return ApiResponse.error("RESERVATION_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(SkuNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleSkuNotFound(SkuNotFoundException ex) {
        return ApiResponse.error("SKU_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(OrderAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiResponse<Void> handleOrderAlreadyExists(OrderAlreadyExistsException ex) {
        return ApiResponse.error("ORDER_ALREADY_EXISTS", ex.getMessage());
    }

    @ExceptionHandler(SystemBusyException.class)
    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public ApiResponse<Void> handleSystemBusy(SystemBusyException ex) {
        return ApiResponse.error("SYSTEM_BUSY", ex.getMessage());
    }

    // Covers both JPA lock timeout and DataIntegrityViolationException from a concurrent duplicate orderId insert
    @ExceptionHandler({JpaSystemException.class, DataIntegrityViolationException.class})
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ApiResponse<Void> handleLockTimeout(RuntimeException ex) {
        return ApiResponse.error("LOCK_TIMEOUT", "The system is busy processing another request. Please retry.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ApiResponse.error("VALIDATION_ERROR", message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<Void> handleUnexpected(Exception ex) {
        return ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred");
    }
}
