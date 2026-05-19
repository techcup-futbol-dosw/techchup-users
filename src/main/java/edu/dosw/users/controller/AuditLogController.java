package edu.dosw.users.controller;

import edu.dosw.users.dto.AuditLogResponse;
import edu.dosw.users.mapper.AuditLogMapper;
import edu.dosw.users.service.IAuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for querying audit log entries.
 *
 * <p>Base path: {@code /api/audit-logs}</p>
 *
 * <p>Access restricted to administrators only, as audit logs contain
 * sensitive operational history of the system.</p>
 */
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final IAuditService auditService;
    private final AuditLogMapper auditLogMapper;

    /**
     * Returns all audit log entries associated with the given sport profile.
     *
     * @param sportProfileId identifier of the sport profile
     * @return list of audit log entries ordered as stored
     */
    @GetMapping("/sport-profiles/{sportProfileId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogResponse>> getBySportProfile(
            @PathVariable Long sportProfileId) {
        List<AuditLogResponse> logs = auditService.getLogsForSportProfile(sportProfileId)
                .stream()
                .map(auditLogMapper::toResponse)
                .toList();
        return ResponseEntity.ok(logs);
    }

    /**
     * Returns all audit log entries associated with the given invitation.
     *
     * @param invitationId identifier of the invitation
     * @return list of audit log entries ordered as stored
     */
    @GetMapping("/invitations/{invitationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogResponse>> getByInvitation(
            @PathVariable Long invitationId) {
        List<AuditLogResponse> logs = auditService.getLogsForInvitation(invitationId)
                .stream()
                .map(auditLogMapper::toResponse)
                .toList();
        return ResponseEntity.ok(logs);
    }
}