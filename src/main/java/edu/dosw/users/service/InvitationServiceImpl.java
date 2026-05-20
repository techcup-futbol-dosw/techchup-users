package edu.dosw.users.service;

import edu.dosw.users.client.IdentityServiceClient;
import edu.dosw.users.entity.InvitationEntity;
import edu.dosw.users.enums.AuditAction;
import edu.dosw.users.enums.InvitationStatus;
import edu.dosw.users.exception.BusinessException;
import edu.dosw.users.exception.ResourceNotFoundException;
import edu.dosw.users.mapper.InvitationMapper;
import edu.dosw.users.model.InvitationModel;
import edu.dosw.users.repository.InvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación por defecto de {@link IInvitationService}.
 *
 * <p>Gestiona el ciclo de vida completo de una invitación de equipo. La existencia
 * del usuario se valida a través de {@link IdentityServiceClient} antes de crear
 * una invitación. {@link InvitationMapper} se encarga de la conversión entre
 * entidad y modelo.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class InvitationServiceImpl implements IInvitationService {

    private final InvitationRepository invitationRepository;
    private final IdentityServiceClient identityServiceClient;
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
        return invitationRepository.findByUserId(playerId)
                .stream()
                .map(invitationMapper::toModel)
                .toList();
    }

    /**
     * {@inheritDoc}
     *
     * <p>Valida que el jugador exista en el servicio de identidad y que no haya
     * una invitación pendiente del mismo equipo antes de crear la nueva invitación.</p>
     */
    @Override
    public InvitationModel send(Long playerId, Long teamId) {
        if (!identityServiceClient.userExists(playerId)) {
            throw new ResourceNotFoundException(
                    "Player not found with id: " + playerId);
        }

        boolean hasPending = invitationRepository
                .findByUserIdAndStatus(playerId, InvitationStatus.PENDING.name())
                .stream()
                .anyMatch(inv -> teamId.equals(inv.getTeamId()));
        if (hasPending) {
            throw new BusinessException(
                    "Player " + playerId + " already has a pending invitation from team " + teamId);
        }

        InvitationEntity entity = InvitationEntity.builder()
                .userId(playerId)
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
     * Aplica una transición de estado a una invitación pendiente, la persiste y
     * registra la entrada de auditoría correspondiente.
     *
     * @param id          identificador de la invitación a modificar
     * @param action      transición de estado a aplicar sobre el modelo de invitación
     * @param auditAction acción de auditoría a registrar tras guardar
     * @param details     descripción de auditoría que explica la transición
     * @return modelo de invitación actualizado
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

        InvitationModel saved = invitationMapper.toModel(invitationRepository.save(updated));
        auditService.logInvitation(id, auditAction, details + " (invitation id: " + id + ")");
        return saved;
    }
}
