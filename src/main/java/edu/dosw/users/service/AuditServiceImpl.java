package edu.dosw.users.service;

import edu.dosw.users.entity.AuditLogEntity;
import edu.dosw.users.entity.InvitationEntity;
import edu.dosw.users.entity.SportProfileEntity;
import edu.dosw.users.enums.AuditAction;
import edu.dosw.users.mapper.AuditLogMapper;
import edu.dosw.users.model.AuditLogModel;
import edu.dosw.users.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementación por defecto de {@link IAuditService}.
 *
 * <p>Persiste las entradas de auditoría a través de {@link AuditLogRepository}.
 * Dado que {@link AuditLogMapper} omite las relaciones JPA al convertir a entidad,
 * esta clase establece manualmente las referencias parciales (solo con id) para que
 * Hibernate escriba los valores correctos de clave foránea.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements IAuditService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    /**
     * {@inheritDoc}
     *
     * <p>Crea una entrada de auditoría con una referencia de solo id a {@link SportProfileEntity}
     * para persistir la clave foránea sin cargar el perfil completo.</p>
     */
    @Override
    public void logSportProfile(Long sportProfileId, AuditAction action, String details) {
        AuditLogModel logModel = AuditLogModel.builder()
                .sportProfileId(sportProfileId)
                .action(action)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();
        AuditLogEntity entity = auditLogMapper.toEntity(logModel);
        entity.setSportProfile(SportProfileEntity.builder().id(sportProfileId).build());
        auditLogRepository.save(entity);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Crea una entrada de auditoría con una referencia de solo id a {@link InvitationEntity}
     * para persistir la clave foránea sin cargar la invitación completa.</p>
     */
    @Override
    public void logInvitation(Long invitationId, AuditAction action, String details) {
        AuditLogModel logModel = AuditLogModel.builder()
                .invitationId(invitationId)
                .action(action)
                .timestamp(LocalDateTime.now())
                .details(details)
                .build();
        AuditLogEntity entity = auditLogMapper.toEntity(logModel);
        entity.setInvitation(InvitationEntity.builder().id(invitationId).build());
        auditLogRepository.save(entity);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AuditLogModel> getLogsForSportProfile(Long sportProfileId) {
        return auditLogRepository.findBySportProfile_Id(sportProfileId)
                .stream()
                .map(auditLogMapper::toModel)
                .toList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AuditLogModel> getLogsForInvitation(Long invitationId) {
        return auditLogRepository.findByInvitation_Id(invitationId)
                .stream()
                .map(auditLogMapper::toModel)
                .toList();
    }
}
