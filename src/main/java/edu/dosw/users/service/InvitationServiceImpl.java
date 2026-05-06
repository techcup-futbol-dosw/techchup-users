package edu.dosw.users.service;

import edu.dosw.users.entity.InvitationEntity;
import edu.dosw.users.entity.UserEntity;
import edu.dosw.users.enums.AuditAction;
import edu.dosw.users.enums.InvitationStatus;
import edu.dosw.users.exception.BusinessException;
import edu.dosw.users.exception.ResourceNotFoundException;
import edu.dosw.users.mapper.InvitationMapper;
import edu.dosw.users.model.InvitationModel;
import edu.dosw.users.repository.InvitationRepository;
import edu.dosw.users.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final InvitationMapper invitationMapper;
    private final IAuditService auditService;

    /**
     * {@inheritDoc}
     */
    @Override
    public InvitationModel getById(Long id) {
        return invitationRepository.findById(id)
                .map(invitationMapper::toModel)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invitation not found with id: " + id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<InvitationModel> getByPlayerId(Long playerId) {
        return invitationRepository.findByPlayer_Id(playerId)
                .stream()
                .map(invitationMapper::toModel)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<InvitationModel> getByPlayerIdFiltered(Long playerId, String status) {
        if (status == null || status.isBlank()) {
            return getByPlayerId(playerId);
        }
        return invitationRepository.findByPlayer_IdAndStatus(playerId, status.toUpperCase())
                .stream()
                .map(invitationMapper::toModel)
                .toList();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Validates that the player exists and that there is no pending
     * invitation from the same team before creating the new invitation.</p>
     */
    @Override
    public InvitationModel send(Long playerId, Long teamId) {
        userRepository.findById(playerId)
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
                .player(UserEntity.builder().id(playerId).build())
                .teamId(teamId)
                .status(InvitationStatus.PENDING.name())
                .sentAt(LocalDateTime.now())
                .build();

        InvitationModel saved = invitationMapper.toModel(invitationRepository.save(entity));
        auditService.logInvitation(saved.getId(), AuditAction.CREATE,
                "Invitation sent to player " + playerId + " from team " + teamId);
        return saved;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvitationModel accept(Long id) {
        return respondToInvitation(id, InvitationModel::accept, AuditAction.UPDATE,
                "Invitation accepted");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InvitationModel reject(Long id) {
        return respondToInvitation(id, InvitationModel::reject, AuditAction.UPDATE,
                "Invitation rejected");
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * Applies a state transition to a pending invitation, persists it, and
     * records the corresponding audit log entry.
     *
     * @param id identifier of the invitation to modify
     * @param action state transition to apply to the invitation model
     * @param auditAction audit action to record after saving
     * @param details audit details describing the transition
     * @return updated invitation model
     */
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
