package edu.dosw.users.model;

import edu.dosw.users.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Modelo de dominio que representa una entrada del log de auditoría del sistema.
 *
 * <p>Cada entrada documenta un evento de negocio (creación, actualización o desactivación)
 * asociado a un perfil deportivo o una invitación de equipo. Al menos uno de los dos
 * identificadores de referencia debe ser no nulo.</p>
 *
 * @author CodeForge
 * @since 1.0
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

    /** Identificador único de la entrada de auditoría. */
    private Long id;
    /** Identificador del perfil deportivo relacionado con el evento auditado. */
    private Long sportProfileId;
    /** Identificador de la invitación relacionada con el evento auditado. */
    private Long invitationId;
    /** Tipo de acción que originó esta entrada de auditoría. */
    private AuditAction action;
    /** Fecha y hora en que ocurrió el evento auditado. */
    private LocalDateTime timestamp;
    /** Descripción adicional o contexto del evento auditado. */
    private String details;
}