package edu.dosw.users.repository;

import edu.dosw.users.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link AuditLogEntity} persistence operations.
 *
 * <p>Provides the standard CRUD operations inherited from {@link JpaRepository}
 * and derived queries used to retrieve audit trail entries associated with sport
 * profiles or invitations.</p>
 *
 * @see AuditLogEntity
 */
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {

    /**
     * Finds all audit log entries linked to the given sport profile.
     *
     * @param sportProfileId identifier of the sport profile whose audit entries are requested
     * @return list of audit log entries associated with the sport profile
     */
    List<AuditLogEntity> findBySportProfile_Id(Long sportProfileId);

    /**
     * Finds all audit log entries linked to the given invitation.
     *
     * @param invitationId identifier of the invitation whose audit entries are requested
     * @return list of audit log entries associated with the invitation
     */
    List<AuditLogEntity> findByInvitation_Id(Long invitationId);
}
