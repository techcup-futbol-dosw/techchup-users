package edu.dosw.users.model;

import edu.dosw.users.enums.InvitationStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvitationModelTest {

    @Test
    void isPending_returnsTrue_whenStatusIsPENDING() {
        InvitationModel model = InvitationModel.builder().status(InvitationStatus.PENDING).build();
        assertTrue(model.isPending());
    }

    @Test
    void isPending_returnsFalse_whenStatusIsNotPENDING() {
        InvitationModel model = InvitationModel.builder().status(InvitationStatus.ACCEPTED).build();
        assertFalse(model.isPending());
    }

    @Test
    void accept_setsStatusToACCEPTED_andRespondedAt() {
        InvitationModel model = InvitationModel.builder().status(InvitationStatus.PENDING).build();
        model.accept();
        assertEquals(InvitationStatus.ACCEPTED, model.getStatus());
        assertNotNull(model.getRespondedAt());
    }

    @Test
    void reject_setsStatusToREJECTED_andRespondedAt() {
        InvitationModel model = InvitationModel.builder().status(InvitationStatus.PENDING).build();
        model.reject();
        assertEquals(InvitationStatus.REJECTED, model.getStatus());
        assertNotNull(model.getRespondedAt());
    }

    @Test
    void cancel_setsStatusToCANCELLED_andRespondedAt() {
        InvitationModel model = InvitationModel.builder().status(InvitationStatus.PENDING).build();
        model.cancel();
        assertEquals(InvitationStatus.CANCELLED, model.getStatus());
        assertNotNull(model.getRespondedAt());
    }
}