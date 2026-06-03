package com.fortna.assignment.inventory_reservation.domain.model.state;

import com.fortna.assignment.inventory_reservation.domain.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationStateTest {

    @Test
    void pendingState_confirm_isAllowed() {
        assertThatCode(() -> new PendingState().confirm()).doesNotThrowAnyException();
    }

    @Test
    void pendingState_cancel_isAllowed() {
        assertThatCode(() -> new PendingState().cancel()).doesNotThrowAnyException();
    }

    @Test
    void confirmedState_confirm_throwsInvalidStateTransition() {
        assertThatThrownBy(() -> new ConfirmedState().confirm())
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("already confirmed");
    }

    @Test
    void confirmedState_cancel_throwsInvalidStateTransition() {
        assertThatThrownBy(() -> new ConfirmedState().cancel())
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("confirmed reservation cannot be cancelled");
    }

    @Test
    void cancelledState_confirm_throwsInvalidStateTransition() {
        assertThatThrownBy(() -> new CancelledState().confirm())
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("cancelled reservation cannot be confirmed");
    }

    @Test
    void cancelledState_cancel_throwsInvalidStateTransition() {
        assertThatThrownBy(() -> new CancelledState().cancel())
                .isInstanceOf(InvalidStateTransitionException.class)
                .hasMessageContaining("already cancelled");
    }
}
