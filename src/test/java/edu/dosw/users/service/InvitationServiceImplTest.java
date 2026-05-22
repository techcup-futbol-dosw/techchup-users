package edu.dosw.users.service;

import edu.dosw.users.repository.UserRepository;
import edu.dosw.users.entity.InvitationEntity;
import edu.dosw.users.enums.AuditAction;
import edu.dosw.users.enums.InvitationStatus;
import edu.dosw.users.exception.BusinessException;
import edu.dosw.users.exception.ResourceNotFoundException;
import edu.dosw.users.mapper.InvitationMapper;
import edu.dosw.users.model.InvitationModel;
import edu.dosw.users.repository.InvitationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link InvitationServiceImpl}.
 *
 * <p>Uses mocked repositories, mapper, identity client, and audit service to
 * verify invitation retrieval, creation, duplicate-pending validation, and
 * status transitions for accept, reject, and cancel operations.</p>
 */
@ExtendWith(MockitoExtension.class)
class InvitationServiceImplTest {

    @Mock private InvitationRepository invitationRepository;
    @Mock private UserRepository userRepository;
    @Mock private InvitationMapper invitationMapper;
    @Mock private IAuditService auditService;

    @InjectMocks private InvitationServiceImpl service;

    // ── getById ──────────────────────────────────────────────────────────────

    @Test
    void getById_found_returnsModel() {
        InvitationEntity entity = InvitationEntity.builder().id(1L).build();
        InvitationModel model = InvitationModel.builder().id(1L).build();
        when(invitationRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(invitationMapper.toModel(entity)).thenReturn(model);

        assertEquals(1L, service.getById(1L).getId());
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        when(invitationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(99L));
    }

    // ── getByPlayerId ─────────────────────────────────────────────────────────

    @Test
    void getByPlayerId_returnsMappedList() {
        InvitationEntity e1 = InvitationEntity.builder().id(1L).build();
        InvitationEntity e2 = InvitationEntity.builder().id(2L).build();
        InvitationModel m1 = InvitationModel.builder().id(1L).build();
        InvitationModel m2 = InvitationModel.builder().id(2L).build();
        when(invitationRepository.findByUserId(10L)).thenReturn(List.of(e1, e2));
        when(invitationMapper.toModel(e1)).thenReturn(m1);
        when(invitationMapper.toModel(e2)).thenReturn(m2);

        List<InvitationModel> result = service.getByPlayerId(10L);

        assertEquals(2, result.size());
    }

    // ── send ─────────────────────────────────────────────────────────────────

    @Test
    void send_validRequest_savesAndLogsAndReturnsModel() {
        InvitationEntity savedEntity = InvitationEntity.builder().id(1L).build();
        InvitationModel savedModel = InvitationModel.builder().id(1L)
                .playerId(10L).teamId(5L).status(InvitationStatus.PENDING).build();

        when(userRepository.existsById(10L)).thenReturn(true);
        when(invitationRepository.findByUserIdAndStatus(10L, "PENDING"))
                .thenReturn(List.of());
        when(invitationRepository.save(any())).thenReturn(savedEntity);
        when(invitationMapper.toModel(savedEntity)).thenReturn(savedModel);

        InvitationModel result = service.send(10L, 5L);

        assertEquals(InvitationStatus.PENDING, result.getStatus());
        verify(auditService).logInvitation(eq(1L), eq(AuditAction.CREATE), any());
    }

    @Test
    void send_playerNotFound_throwsResourceNotFoundException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> service.send(99L, 5L));
    }

    @Test
    void send_alreadyHasPendingFromSameTeam_throwsBusinessException() {
        InvitationEntity existing = InvitationEntity.builder()
                .id(3L).teamId(5L).status("PENDING").build();

        when(userRepository.existsById(10L)).thenReturn(true);
        when(invitationRepository.findByUserIdAndStatus(10L, "PENDING"))
                .thenReturn(List.of(existing));

        assertThrows(BusinessException.class, () -> service.send(10L, 5L));
    }

    // ── accept ────────────────────────────────────────────────────────────────

    @Test
    void accept_pendingInvitation_changesStatusAndLogs() {
        InvitationEntity existing = InvitationEntity.builder()
                .id(1L).userId(10L).teamId(5L).status("PENDING").build();
        InvitationModel pendingModel = InvitationModel.builder()
                .id(1L).status(InvitationStatus.PENDING).build();
        when(invitationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(invitationMapper.toModel(existing)).thenReturn(pendingModel);
        when(invitationMapper.toEntity(pendingModel)).thenReturn(mock(InvitationEntity.class));
        when(invitationRepository.save(any())).thenReturn(existing);
        when(invitationMapper.toModel(existing)).thenReturn(pendingModel);

        service.accept(1L);

        assertEquals(InvitationStatus.ACCEPTED, pendingModel.getStatus());
        assertNotNull(pendingModel.getRespondedAt());
        verify(auditService).logInvitation(eq(1L), eq(AuditAction.UPDATE), any());
    }

    @Test
    void accept_nonPendingInvitation_throwsBusinessException() {
        InvitationEntity existing = InvitationEntity.builder().id(1L).userId(1L).build();
        InvitationModel alreadyAccepted = InvitationModel.builder()
                .id(1L).status(InvitationStatus.ACCEPTED).build();

        when(invitationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(invitationMapper.toModel(existing)).thenReturn(alreadyAccepted);

        assertThrows(BusinessException.class, () -> service.accept(1L));
    }

    @Test
    void accept_notFound_throwsResourceNotFoundException() {
        when(invitationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.accept(99L));
    }

    // ── reject ────────────────────────────────────────────────────────────────

    @Test
    void reject_pendingInvitation_changesStatus() {
        InvitationEntity existing = InvitationEntity.builder().id(2L).userId(10L).build();
        InvitationModel pendingModel = InvitationModel.builder()
                .id(2L).status(InvitationStatus.PENDING).build();
        when(invitationRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(invitationMapper.toModel(existing)).thenReturn(pendingModel);
        when(invitationMapper.toEntity(pendingModel)).thenReturn(mock(InvitationEntity.class));
        when(invitationRepository.save(any())).thenReturn(existing);

        service.reject(2L);

        assertEquals(InvitationStatus.REJECTED, pendingModel.getStatus());
    }

    // ── cancel ────────────────────────────────────────────────────────────────

    @Test
    void cancel_pendingInvitation_changesStatus() {
        InvitationEntity existing = InvitationEntity.builder().id(3L).userId(10L).build();
        InvitationModel pendingModel = InvitationModel.builder()
                .id(3L).status(InvitationStatus.PENDING).build();
        when(invitationRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(invitationMapper.toModel(existing)).thenReturn(pendingModel);
        when(invitationMapper.toEntity(pendingModel)).thenReturn(mock(InvitationEntity.class));
        when(invitationRepository.save(any())).thenReturn(existing);

        service.cancel(3L);

        assertEquals(InvitationStatus.CANCELLED, pendingModel.getStatus());
    }
}
