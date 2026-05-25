package edu.dosw.users.controller;

import edu.dosw.users.dto.InvitationResponse;
import edu.dosw.users.mapper.InvitationMapper;
import edu.dosw.users.service.IInvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador REST para la gestión de invitaciones a equipos.
 *
 * <p>Ruta base: {@code /api/invitations}</p>
 *
 * <p>Resumen de control de acceso:
 * <ul>
 *   <li>CAPTAIN — puede enviar y cancelar invitaciones.</li>
 *   <li>PLAYER (propietario) — puede consultar, aceptar y rechazar sus propias invitaciones.</li>
 *   <li>ADMIN — acceso completo a todos los endpoints de invitación.</li>
 * </ul>
 * </p>
 *
 * @author CodeForge
 * @since 1.0
 */
@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final IInvitationService invitationService;
    private final InvitationMapper invitationMapper;

    /**
     * Retorna la invitación con el identificador indicado.
     *
     * <p>Accesible por cualquier usuario autenticado; la capa de servicio impone
     * las reglas de propiedad sobre el recurso.</p>
     *
     * @param id identificador de la invitación
     * @return respuesta con los datos de la invitación
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InvitationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                invitationMapper.toResponse(invitationService.getById(id)));
    }

    /**
     * Retorna todas las invitaciones recibidas por el usuario indicado.
     *
     * <p>Solo el propietario de las invitaciones o un administrador puede listarlas.</p>
     *
     * @param userId identificador del usuario jugador
     * @return lista de invitaciones asociadas al usuario
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<InvitationResponse>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(
                invitationService.getByPlayerId(userId).stream()
                        .map(invitationMapper::toResponse)
                        .toList());
    }

    /**
     * Envía una nueva invitación de un equipo a un jugador.
     *
     * <p>Solo los capitanes pueden enviar invitaciones, ya que son quienes
     * gestionan el plantel de su equipo.</p>
     *
     * @param userId identificador del jugador invitado
     * @param teamId identificador del equipo que envía la invitación
     * @return respuesta con los datos de la invitación creada (HTTP 201)
     */
    @PostMapping("/user/{userId}/team/{teamId}")
    @PreAuthorize("hasRole('CAPTAIN')")
    public ResponseEntity<InvitationResponse> send(
            @PathVariable Long userId,
            @PathVariable Long teamId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invitationMapper.toResponse(invitationService.send(userId, teamId)));
    }

    /**
     * Acepta una invitación pendiente.
     *
     * <p>Solo el jugador invitado (propietario) o un administrador puede aceptar.</p>
     *
     * @param id identificador de la invitación
     * @return respuesta con los datos de la invitación actualizada
     */
    @PatchMapping("/{id}/accept")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InvitationResponse> accept(@PathVariable Long id) {
        return ResponseEntity.ok(
                invitationMapper.toResponse(invitationService.accept(id)));
    }

    /**
     * Rechaza una invitación pendiente.
     *
     * <p>Solo el jugador invitado (propietario) o un administrador puede rechazar.</p>
     *
     * @param id identificador de la invitación
     * @return respuesta con los datos de la invitación actualizada
     */
    @PatchMapping("/{id}/reject")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InvitationResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(
                invitationMapper.toResponse(invitationService.reject(id)));
    }

    /**
     * Cancela una invitación pendiente.
     *
     * <p>Solo el capitán que la envió puede cancelarla.</p>
     *
     * @param id identificador de la invitación
     * @return respuesta con los datos de la invitación cancelada
     */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CAPTAIN')")
    public ResponseEntity<InvitationResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(
                invitationMapper.toResponse(invitationService.cancel(id)));
    }
}