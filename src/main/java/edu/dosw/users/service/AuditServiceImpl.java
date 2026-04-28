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
 * Default implementation of {@link IAuditService}.
 *
 * <p>Persists audit entries via {@link AuditLogRepository}. Because the
 * {@link AuditLogMapper} ignores the JPA relationships on {@code toEntity},
 * this class manually sets the partial entity references (id-only) so that
 * Hibernate writes the correct foreign-key values.</p>
 */
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements IAuditService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

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

    @Override
    public List<AuditLogModel> getLogsForSportProfile(Long sportProfileId) {
        return auditLogRepository.findBySportProfile_Id(sportProfileId)
                .stream()
                .map(auditLogMapper::toModel)
                .toList();
    }

    @Override
    public List<AuditLogModel> getLogsForInvitation(Long invitationId) {
        return auditLogRepository.findByInvitation_Id(invitationId)
                .stream()
                .map(auditLogMapper::toModel)
                .toList();
    }
}