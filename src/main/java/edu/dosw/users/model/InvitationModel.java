package edu.dosw.users.model;

import edu.dosw.users.enums.InvitationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Modelo de dominio que representa una invitación enviada a un jugador para unirse
 * a un equipo en la plataforma TechCup Fútbol.
 *
 * <p>Encapsula el ciclo de vida de la invitación a través de los métodos
 * {@link #accept()}, {@link #reject()} y {@link #cancel()}, que actualizan
 * automáticamente el estado y registran la marca de tiempo de respuesta.</p>
 *
 * @author CodeForge
 * @since 1.0
 * @see edu.dosw.users.entity.InvitationEntity
 * @see edu.dosw.users.mapper.InvitationMapper
 * @see InvitationStatus
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationModel {

    /** Identificador único de la invitación. */
    private Long id;
    /** Identificador del jugador que recibe la invitación. */
    private Long playerId;
    /** Identificador del equipo que extiende la invitación. */
    private Long teamId;
    /** Estado actual de la invitación en su ciclo de vida. */
    private InvitationStatus status;
    /** Fecha y hora en que se envió la invitación. */
    private LocalDateTime sentAt;
    /** Fecha y hora en que el jugador o capitán respondió a la invitación. */
    private LocalDateTime respondedAt;

    /**
     * Indica si la invitación está a la espera de una respuesta.
     *
     * @return {@code true} si el estado es {@link InvitationStatus#PENDING}
     */
    public boolean isPending() {
        return InvitationStatus.PENDING == status;
    }

    /**
     * Acepta la invitación, cambiando el estado a {@link InvitationStatus#ACCEPTED}
     * y registrando la marca de tiempo de respuesta.
     */
    public void accept() {
        this.status = InvitationStatus.ACCEPTED;
        this.respondedAt = LocalDateTime.now();
    }

    /**
     * Rechaza la invitación, cambiando el estado a {@link InvitationStatus#REJECTED}
     * y registrando la marca de tiempo de respuesta.
     */
    public void reject() {
        this.status = InvitationStatus.REJECTED;
        this.respondedAt = LocalDateTime.now();
    }

    /**
     * Cancela la invitación, cambiando el estado a {@link InvitationStatus#CANCELLED}
     * y registrando la marca de tiempo de cancelación.
     */
    public void cancel() {
        this.status = InvitationStatus.CANCELLED;
        this.respondedAt = LocalDateTime.now();
    }
}