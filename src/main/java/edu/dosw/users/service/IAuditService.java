package edu.dosw.users.service;

import edu.dosw.users.enums.AuditAction;
import edu.dosw.users.model.AuditLogModel;

import java.util.List;

/**
 * Servicio para registrar y consultar entradas del log de auditoría.
 *
 * <p>Todo evento de negocio relevante (creación, actualización o desactivación de
 * un perfil deportivo o una invitación) debe registrarse a través de este servicio
 * para mantener la trazabilidad del historial del sistema.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
public interface IAuditService {

    /**
     * Registra un evento de auditoría asociado a un perfil deportivo.
     *
     * @param sportProfileId identificador del perfil deportivo afectado
     * @param action         tipo de acción realizada
     * @param details        descripción adicional del evento
     */
    void logSportProfile(Long sportProfileId, AuditAction action, String details);

    /**
     * Registra un evento de auditoría asociado a una invitación de equipo.
     *
     * @param invitationId identificador de la invitación afectada
     * @param action       tipo de acción realizada
     * @param details      descripción adicional del evento
     */
    void logInvitation(Long invitationId, AuditAction action, String details);

    /**
     * Retorna todas las entradas de auditoría relacionadas con el perfil deportivo indicado.
     *
     * @param sportProfileId identificador del perfil deportivo
     * @return lista de modelos de auditoría coincidentes; puede estar vacía
     */
    List<AuditLogModel> getLogsForSportProfile(Long sportProfileId);

    /**
     * Retorna todas las entradas de auditoría relacionadas con la invitación indicada.
     *
     * @param invitationId identificador de la invitación
     * @return lista de modelos de auditoría coincidentes; puede estar vacía
     */
    List<AuditLogModel> getLogsForInvitation(Long invitationId);
}