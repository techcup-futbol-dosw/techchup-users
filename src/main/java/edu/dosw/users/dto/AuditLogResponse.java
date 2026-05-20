package edu.dosw.users.dto;

import edu.dosw.users.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Vista de solo lectura de una entrada del log de auditoría retornada por la API.
 *
 * @author CodeForge
 * @since 1.0
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