package edu.dosw.users.service;

import edu.dosw.users.model.InvitationModel;

import java.util.List;

/**
 * Servicio para gestionar las invitaciones de equipo enviadas a jugadores.
 *
 * <p>Gestiona el ciclo de vida completo de una invitación: envío, aceptación,
 * rechazo y cancelación. Cada cambio de estado queda registrado en el log de auditoría.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
public interface IInvitationService {

    /**
     * Recupera una invitación por su identificador.
     *
     * @param id identificador de la invitación
     * @return modelo de la invitación correspondiente
     * @throws edu.dosw.users.exception.ResourceNotFoundException si no se encuentra la invitación
     */
    InvitationModel getById(Long id);

    /**
     * Retorna todas las invitaciones recibidas por el jugador indicado.
     *
     * @param playerId identificador del jugador
     * @return lista de modelos de invitación coincidentes; puede estar vacía
     */
    List<InvitationModel> getByPlayerId(Long playerId);

    /**
     * Envía una nueva invitación al jugador indicado desde el equipo especificado.
     *
     * @param playerId identificador del jugador que recibe la invitación
     * @param teamId   identificador del equipo que extiende la invitación
     * @return modelo de la invitación guardada
     * @throws edu.dosw.users.exception.ResourceNotFoundException si el jugador no existe
     * @throws edu.dosw.users.exception.BusinessException         si el jugador ya tiene una invitación pendiente del mismo equipo
     */
    InvitationModel send(Long playerId, Long teamId);

    /**
     * Acepta una invitación pendiente.
     *
     * @param id identificador de la invitación
     * @return modelo de la invitación actualizada
     * @throws edu.dosw.users.exception.ResourceNotFoundException si no se encuentra la invitación
     * @throws edu.dosw.users.exception.BusinessException         si la invitación no está en estado {@code PENDING}
     */
    InvitationModel accept(Long id);

    /**
     * Rechaza una invitación pendiente.
     *
     * @param id identificador de la invitación
     * @return modelo de la invitación actualizada
     * @throws edu.dosw.users.exception.ResourceNotFoundException si no se encuentra la invitación
     * @throws edu.dosw.users.exception.BusinessException         si la invitación no está en estado {@code PENDING}
     */
    InvitationModel reject(Long id);

    /**
     * Cancela una invitación pendiente.
     *
     * @param id identificador de la invitación
     * @return modelo de la invitación actualizada
     * @throws edu.dosw.users.exception.ResourceNotFoundException si no se encuentra la invitación
     * @throws edu.dosw.users.exception.BusinessException         si la invitación no está en estado {@code PENDING}
     */
    InvitationModel cancel(Long id);
}