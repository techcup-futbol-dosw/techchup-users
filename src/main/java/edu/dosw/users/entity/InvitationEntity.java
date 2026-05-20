package edu.dosw.users.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una invitación de equipo en la base de datos relacional.
 *
 * <p>Mapeada a la tabla {@code invitations}. Registra qué jugador recibió la invitación,
 * a qué equipo pertenece, el estado de la invitación y las marcas de tiempo de envío
 * y respuesta.</p>
 *
 * @author CodeForge
 * @since 1.0
 * @see edu.dosw.users.model.InvitationModel
 * @see edu.dosw.users.mapper.InvitationMapper
 */
@Entity
@Table(name = "invitations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationEntity {

    /** Identificador único generado automáticamente por la base de datos. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador del equipo que extiende la invitación (referencia externa). */
    @Column(name = "team_id", nullable = false)
    private Long teamId;

    /** Estado de la invitación almacenado como {@code String} (p. ej. {@code "PENDING"}). */
    @Column(name = "status", nullable = false)
    private String status;

    /** Fecha y hora en que se envió la invitación al jugador. */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    /** Fecha y hora en que el jugador o capitán respondió a la invitación. */
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    /**
     * Identificador del jugador que recibe la invitación.
     * Gestionado por el servicio de identidad; almacenado como columna simple sin relación JPA.
     */
    @Column(name = "player_id", nullable = false)
    private Long userId;
}
