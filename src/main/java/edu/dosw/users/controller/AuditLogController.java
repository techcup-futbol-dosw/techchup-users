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
 * Controlador REST para la consulta de entradas del registro de auditoría.
 *
 * <p>Ruta base: {@code /api/audit-logs}</p>
 *
 * <p>El acceso está restringido exclusivamente a administradores, ya que los
 * registros de auditoría contienen el historial operativo sensible del sistema.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final IAuditService auditService;
    private final AuditLogMapper auditLogMapper;

    /**
     * Retorna todas las entradas de auditoría asociadas al perfil deportivo indicado.
     *
     * @param sportProfileId identificador del perfil deportivo
     * @return lista de entradas de auditoría en el orden de almacenamiento
     */
    @GetMapping("/sport-profiles/{sportProfileId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<AuditLogResponse>> getBySportProfile(
            @PathVariable Long sportProfileId) {
        List<AuditLogResponse> logs = auditService.getLogsForSportProfile(sportProfileId)
                .stream()
                .map(auditLogMapper::toResponse)
                .toList();
        return ResponseEntity.ok(logs);
    }

    /**
     * Retorna todas las entradas de auditoría asociadas a la invitación indicada.
     *
     * @param invitationId identificador de la invitación
     * @return lista de entradas de auditoría en el orden de almacenamiento
     */
    @GetMapping("/invitations/{invitationId}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<AuditLogResponse>> getByInvitation(
            @PathVariable Long invitationId) {
        List<AuditLogResponse> logs = auditService.getLogsForInvitation(invitationId)
                .stream()
                .map(auditLogMapper::toResponse)
                .toList();
        return ResponseEntity.ok(logs);
    }
}