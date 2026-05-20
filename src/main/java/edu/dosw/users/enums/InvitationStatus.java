package edu.dosw.users.enums;

/**
 * Estados posibles durante el ciclo de vida de una invitación de equipo.
 *
 * <p>Las transiciones de estado se realizan a través de los métodos
 * {@code InvitationModel#accept()}, {@code InvitationModel#reject()} y
 * {@code InvitationModel#cancel()}.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
public enum InvitationStatus {
    /** La invitación ha sido enviada y aún no ha recibido respuesta del jugador. */
    PENDING,
    /** El jugador aceptó la invitación y se unió al equipo. */
    ACCEPTED,
    /** El jugador rechazó la invitación. */
    REJECTED,
    /** El capitán o administrador canceló la invitación antes de que fuera respondida. */
    CANCELLED
}