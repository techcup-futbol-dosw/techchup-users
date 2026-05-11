package edu.dosw.users.model;

import edu.dosw.users.enums.InvitationStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the business methods of {@link InvitationModel}.
 *
 * <p>Verifies the behaviour of {@link InvitationModel#isPending()},
 * {@link InvitationModel#accept()}, {@link InvitationModel#reject()} and
 * {@link InvitationModel#cancel()}, including the status update and the
 * automatic recording of the response timestamp.</p>
 */
class InvitationModelTest {

    /**
     * Verifies that {@code isPending} returns {@code true} when the status
     * is {@link InvitationStatus#PENDING}.
     */
    @Test
    void isPending_returnsTrue_whenStatusIsPENDING() {
        InvitationModel model = InvitationModel.builder().status(InvitationStatus.PENDING).build();
        assertTrue(model.isPending());
    }

    /**
     * Verifies that {@code isPending} returns {@code false} when the status
     * is different from {@link InvitationStatus#PENDING}.
     */
    @Test
    void isPending_returnsFalse_whenStatusIsNotPENDING() {
        InvitationModel model = InvitationModel.builder().status(InvitationStatus.ACCEPTED).build();
        assertFalse(model.isPending());
    }

    /**
     * Verifies that {@code accept} changes the status to {@link InvitationStatus#ACCEPTED}
     * and sets {@code respondedAt} to a non-null value.
     */
    @Test
    void accept_setsStatusToACCEPTED_andRespondedAt() {
        InvitationModel model = InvitationModel.builder().status(InvitationStatus.PENDING).build();
        model.accept();
        assertEquals(InvitationStatus.ACCEPTED, model.getStatus());
        assertNotNull(model.getRespondedAt());
    }

    /**
     * Verifies that {@code reject} changes the status to {@link InvitationStatus#REJECTED}
     * and sets {@code respondedAt} to a non-null value.
     */
    @Test
    void reject_setsStatusToREJECTED_andRespondedAt() {
        InvitationModel model = InvitationModel.builder().status(InvitationStatus.PENDING).build();
        model.reject();
        assertEquals(InvitationStatus.REJECTED, model.getStatus());
        assertNotNull(model.getRespondedAt());
    }

    /**
     * Verifies that {@code cancel} changes the status to {@link InvitationStatus#CANCELLED}
     * and sets {@code respondedAt} to a non-null value.
     */
    @Test
    void cancel_setsStatusToCANCELLED_andRespondedAt() {
        InvitationModel model = InvitationModel.builder().status(InvitationStatus.PENDING).build();
        model.cancel();
        assertEquals(InvitationStatus.CANCELLED, model.getStatus());
        assertNotNull(model.getRespondedAt());
    }
}