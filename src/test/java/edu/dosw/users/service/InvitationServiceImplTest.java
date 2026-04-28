package edu.dosw.users.service;

import edu.dosw.users.entity.InvitationEntity;
import edu.dosw.users.entity.UserProfileEntity;
import edu.dosw.users.enums.AuditAction;
import edu.dosw.users.enums.InvitationStatus;
import edu.dosw.users.exception.BusinessException;
import edu.dosw.users.exception.ResourceNotFoundException;
import edu.dosw.users.mapper.InvitationMapper;
import edu.dosw.users.model.InvitationModel;
import edu.dosw.users.repository.InvitationRepository;
import edu.dosw.users.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvitationServiceImplTest {

    @Mock private InvitationRepository invitationRepository;
    @Mock private UserProfileRepository userProfileRepository;
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
        when(invitationRepository.findByPlayer_Id(10L)).thenReturn(List.of(e1, e2));
        when(invitationMapper.toModel(e1)).thenReturn(m1);
        when(invitationMapper.toModel(e2)).thenReturn(m2);

        List<InvitationModel> result = service.getByPlayerId(10L);

        assertEquals(2, result.size());
    }

    // ── send ─────────────────────────────────────────────────────────────────

    @Test
    void send_validRequest_savesAndLogsAndReturnsModel() {
        UserProfileEntity player = UserProfileEntity.builder().id(10L).build();
        InvitationEntity savedEntity = InvitationEntity.builder().id(1L).build();
        InvitationModel savedModel = InvitationModel.builder().id(1L)
                .playerId(10L).teamId(5L).status(InvitationStatus.PENDING).build();

        when(userProfileRepository.findById(10L)).thenReturn(Optional.of(player));
        when(invitationRepository.findByPlayer_IdAndStatus(10L, "PENDING"))
                .thenReturn(List.of());
        when(invitationRepository.save(any())).thenReturn(savedEntity);
        when(invitationMapper.toModel(savedEntity)).thenReturn(savedModel);

        InvitationModel result = service.send(10L, 5L);

        assertEquals(InvitationStatus.PENDING, result.getStatus());
        verify(auditService).logInvitation(eq(1L), eq(AuditAction.CREATE), any());
    }

    @Test
    void send_playerNotFound_throwsResourceNotFoundException() {
        when(userProfileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.send(99L, 5L));
    }

    @Test
    void send_alreadyHasPendingFromSameTeam_throwsBusinessException() {
        UserProfileEntity player = UserProfileEntity.builder().id(10L).build();
        InvitationEntity existing = InvitationEntity.builder()
                .id(3L).teamId(5L).status("PENDING").build();

        when(userProfileRepository.findById(10L)).thenReturn(Optional.of(player));
        when(invitationRepository.findByPlayer_IdAndStatus(10L, "PENDING"))
                .thenReturn(List.of(existing));

        assertThrows(BusinessException.class, () -> service.send(10L, 5L));
    }

    // ── accept ────────────────────────────────────────────────────────────────

    @Test
    void accept_pendingInvitation_changesStatusAndLogs() {
        UserProfileEntity player = UserProfileEntity.builder().id(10L).build();
        InvitationEntity existing = InvitationEntity.builder()
                .id(1L).player(player).teamId(5L).status("PENDING").build();
        InvitationModel pendingModel = InvitationModel.builder()
                .id(1L).status(InvitationStatus.PENDING).build();
        InvitationEntity updatedEntity = InvitationEntity.builder().id(1L).build();
        InvitationModel acceptedModel = InvitationModel.builder()
                .id(1L).status(InvitationStatus.ACCEPTED)
                .respondedAt(LocalDateTime.now()).build();

        when(invitationRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(invitationMapper.toModel(existing)).thenReturn(pendingModel);
        when(invitationMapper.toEntity(pendingModel)).thenReturn(updatedEntity);
        when(invitationRepository.save(any())).thenReturn(updatedEntity);
        when(invitationMapper.toModel(updatedEntity)).thenReturn(acceptedModel);

        InvitationModel result = service.accept(1L);

        assertEquals(InvitationStatus.ACCEPTED, pendingModel.getStatus());
        assertNotNull(pendingModel.getRespondedAt());
        verify(auditService).logInvitation(eq(1L), eq(AuditAction.UPDATE), any());
    }

    @Test
    void accept_nonPendingInvitation_throwsBusinessException() {
        InvitationEntity existing = InvitationEntity.builder()
                .id(1L).player(UserProfileEntity.builder().id(1L).build()).build();
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
        UserProfileEntity player = UserProfileEntity.builder().id(10L).build();
        InvitationEntity existing = InvitationEntity.builder()
                .id(2L).player(player).build();
        InvitationModel pendingModel = InvitationModel.builder()
                .id(2L).status(InvitationStatus.PENDING).build();
        InvitationEntity updatedEntity = InvitationEntity.builder().id(2L).build();
        InvitationModel rejectedModel = InvitationModel.builder()
                .id(2L).status(InvitationStatus.REJECTED).build();

        when(invitationRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(invitationMapper.toModel(existing)).thenReturn(pendingModel);
        when(invitationMapper.toEntity(pendingModel)).thenReturn(updatedEntity);
        when(invitationRepository.save(any())).thenReturn(updatedEntity);
        when(invitationMapper.toModel(updatedEntity)).thenReturn(rejectedModel);

        InvitationModel result = service.reject(2L);

        assertEquals(InvitationStatus.REJECTED, pendingModel.getStatus());
    }

    // ── cancel ────────────────────────────────────────────────────────────────

    @Test
    void cancel_pendingInvitation_changesStatus() {
        UserProfileEntity player = UserProfileEntity.builder().id(10L).build();
        InvitationEntity existing = InvitationEntity.builder()
                .id(3L).player(player).build();
        InvitationModel pendingModel = InvitationModel.builder()
                .id(3L).status(InvitationStatus.PENDING).build();
        InvitationEntity updatedEntity = InvitationEntity.builder().id(3L).build();
        InvitationModel cancelledModel = InvitationModel.builder()
                .id(3L).status(InvitationStatus.CANCELLED).build();

        when(invitationRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(invitationMapper.toModel(existing)).thenReturn(pendingModel);
        when(invitationMapper.toEntity(pendingModel)).thenReturn(updatedEntity);
        when(invitationRepository.save(any())).thenReturn(updatedEntity);
        when(invitationMapper.toModel(updatedEntity)).thenReturn(cancelledModel);

        InvitationModel result = service.cancel(3L);

        assertEquals(InvitationStatus.CANCELLED, pendingModel.getStatus());
    }
}