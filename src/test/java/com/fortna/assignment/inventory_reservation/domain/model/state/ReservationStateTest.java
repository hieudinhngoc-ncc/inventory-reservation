package com.fortna.assignment.inventory_reservation.domain.model.state;

import com.fortna.assignment.inventory_reservation.domain.model.Reservation;
import com.fortna.assignment.inventory_reservation.domain.model.ReservationStatus;
import com.fortna.assignment.inventory_reservation.domain.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationStateTest {

    @Test
    void pendingState_confirm_isAllowed() {
        Reservation reservation = new Reservation();
        PendingState.INSTANCE.confirm(reservation);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    void pendingState_cancel_isAllowed() {
        Reservation reservation = new Reservation();
        PendingState.INSTANCE.cancel(reservation);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void confirmedState_confirm_throwsInvalidStateTransition() {
        assertThatThrownBy(() -> ConfirmedState.INSTANCE.confirm(new Reservation()))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("already confirmed");
    }

    @Test
    void confirmedState_cancel_throwsInvalidStateTransition() {
        assertThatThrownBy(() -> ConfirmedState.INSTANCE.cancel(new Reservation()))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("confirmed reservation cannot be cancelled");
    }

    @Test
    void cancelledState_confirm_throwsInvalidStateTransition() {
        assertThatThrownBy(() -> CancelledState.INSTANCE.confirm(new Reservation()))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("cancelled reservation cannot be confirmed");
    }

    @Test
    void cancelledState_cancel_throwsInvalidStateTransition() {
        assertThatThrownBy(() -> CancelledState.INSTANCE.cancel(new Reservation()))
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("already cancelled");
    }
}
