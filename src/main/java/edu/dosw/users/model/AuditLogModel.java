package edu.dosw.users.model;

import edu.dosw.users.enums.AuditAction;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AuditLogModel {

    private Long id;
    private Long sportProfileId;
    private Long invitationId;
    private AuditAction action;
    private LocalDateTime timestamp;
    private String details;
}