package com.fortna.assignment.inventory_reservation.domain.model;

import com.fortna.assignment.inventory_reservation.domain.model.state.CancelledState;
import com.fortna.assignment.inventory_reservation.domain.model.state.ConfirmedState;
import com.fortna.assignment.inventory_reservation.domain.model.state.PendingState;
import com.fortna.assignment.inventory_reservation.domain.model.state.ReservationState;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ReservationItem> items = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void confirm() {
        currentState().confirm(this);
    }

    public void cancel() {
        currentState().cancel(this);
    }

    // Resolves the current State Pattern implementation based on the persisted status value
    private ReservationState currentState() {
        return switch (this.status) {
            case PENDING -> PendingState.INSTANCE;
            case CONFIRMED -> ConfirmedState.INSTANCE;
            case CANCELLED -> CancelledState.INSTANCE;
        };
    }
}
