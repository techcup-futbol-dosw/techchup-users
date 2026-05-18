package edu.dosw.users.dto;

import edu.dosw.users.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Read-only view of an audit log entry returned by the API.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private Long id;
    private Long sportProfileId;
    private Long invitationId;
    private AuditAction action;
    private LocalDateTime timestamp;
    private String details;
}