package edu.dosw.users.enums;

/**
 * Tipos de acción que pueden registrarse en el log de auditoría del sistema.
 *
 * <p>Cada valor corresponde a un evento de negocio que modifica el estado
 * de un perfil deportivo o una invitación de equipo.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
public enum AuditAction {
    /** Creación de un perfil deportivo o una invitación. */
    CREATE,
    /** Actualización de datos de un perfil deportivo o una invitación. */
    UPDATE,
    /** Desactivación de un perfil deportivo (sin eliminación física). */
    DEACTIVATE
}