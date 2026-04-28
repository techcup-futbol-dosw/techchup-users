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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Default implementation of {@link IInvitationService}.
 *
 * <p>Manages the full lifecycle of a team invitation. Because
 * {@link InvitationMapper} ignores the {@code player} relationship,
 * this class sets it manually using a partial entity reference (id only)
 * after mapping.</p>
 */
@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements IInvitationService {

    private final InvitationRepository invitationRepository;
    private final UserProfileRepository userProfileRepository;
    private final InvitationMapper invitationMapper;
    private final IAuditService auditService;

    @Override
    public InvitationModel getById(Long id) {
        return invitationRepository.findById(id)
                .map(invitationMapper::toModel)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invitation not found with id: " + id));
    }

    @Override
    public List<InvitationModel> getByPlayerId(Long playerId) {
        return invitationRepository.findByPlayer_Id(playerId)
                .stream()
                .map(invitationMapper::toModel)
                .toList();
    }

    @Override
    public InvitationModel send(Long playerId, Long teamId) {
        userProfileRepository.findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Player not found with id: " + playerId));

        boolean hasPending = invitationRepository
                .findByPlayer_IdAndStatus(playerId, InvitationStatus.PENDING.name())
                .stream()
                .anyMatch(inv -> teamId.equals(inv.getTeamId()));
        if (hasPending) {
            throw new BusinessException(
                    "Player " + playerId + " already has a pending invitation from team " + teamId);
        }

        InvitationEntity entity = InvitationEntity.builder()
                .player(UserProfileEntity.builder().id(playerId).build())
                .teamId(teamId)
                .status(InvitationStatus.PENDING.name())
                .sentAt(LocalDateTime.now())
                .build();

        InvitationModel saved = invitationMapper.toModel(invitationRepository.save(entity));
        auditService.logInvitation(saved.getId(), AuditAction.CREATE,
                "Invitation sent to player " + playerId + " from team " + teamId);
        return saved;
    }

    @Override
    public InvitationModel accept(Long id) {
        return respondToInvitation(id, InvitationModel::accept, AuditAction.UPDATE,
                "Invitation accepted");
    }

    @Override
    public InvitationModel reject(Long id) {
        return respondToInvitation(id, InvitationModel::reject, AuditAction.UPDATE,
                "Invitation rejected");
    }

    @Override
    public InvitationModel cancel(Long id) {
        return respondToInvitation(id, InvitationModel::cancel, AuditAction.UPDATE,
                "Invitation cancelled");
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    @FunctionalInterface
    private interface InvitationAction {
        void apply(InvitationModel model);
    }

    private InvitationModel respondToInvitation(Long id, InvitationAction action,
                                                AuditAction auditAction, String details) {
        InvitationEntity existing = invitationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invitation not found with id: " + id));

        InvitationModel model = invitationMapper.toModel(existing);
        if (!model.isPending()) {
            throw new BusinessException(
                    "Cannot modify invitation " + id + " — current status: " + model.getStatus());
        }

        action.apply(model);

        InvitationEntity updated = invitationMapper.toEntity(model);
        updated.setPlayer(existing.getPlayer());

        InvitationModel saved = invitationMapper.toModel(invitationRepository.save(updated));
        auditService.logInvitation(id, auditAction, details + " (invitation id: " + id + ")");
        return saved;
    }
}