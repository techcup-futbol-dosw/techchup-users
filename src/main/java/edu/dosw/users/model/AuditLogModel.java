package edu.dosw.users.model;

import edu.dosw.users.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Domain model representing a system audit log entry.
 *
 * <p>Each entry documents a business event (creation, update or deactivation)
 * associated with a sport profile or a team invitation. At least one of the
 * two reference identifiers must be non-null.</p>
 *
 * @see edu.dosw.users.entity.AuditLogEntity
 * @see edu.dosw.users.mapper.AuditLogMapper
 * @see AuditAction
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogModel {

    /** Unique identifier of the audit log entry. */
    private Long id;
    /** Identifier of the sport profile related to the audited event. */
    private Long sportProfileId;
    /** Identifier of the invitation related to the audited event. */
    private Long invitationId;
    /** Type of action that originated this audit log entry. */
    private AuditAction action;
    /** Date and time when the audited event occurred. */
    private LocalDateTime timestamp;
    /** Additional description or context of the audited event. */
    private String details;
}