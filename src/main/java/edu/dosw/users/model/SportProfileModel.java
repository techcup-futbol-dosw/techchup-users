package edu.dosw.users.model;

import edu.dosw.users.enums.Position;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Modelo de dominio que representa el perfil deportivo de un jugador.
 *
 * <p>Contiene la posición en el campo, el número de dorsal, la referencia al documento
 * de foto almacenado en MongoDB y la disponibilidad del jugador para ser convocado.
 * No puede eliminarse ni modificarse mientras el jugador esté asignado a un equipo activo.</p>
 *
 * @author CodeForge
 * @since 1.0
 * @see edu.dosw.users.entity.SportProfileEntity
 * @see edu.dosw.users.mapper.SportProfileMapper
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SportProfileModel {

    /** Identificador único del perfil deportivo. */
    private Long id;
    /** Identificador del usuario propietario de este perfil deportivo. */
    private Long userId;
    /** Posición táctica del jugador en el campo. */
    private Position position;
    /** Número de dorsal del jugador. */
    private Integer dorsalNumber;
    /** Identificador del documento {@code PlayerPhoto} en MongoDB. */
    private String photoId;
    /** Indica si el jugador está disponible para participar en torneos. */
    private boolean available;
    /** Fecha y hora en que se creó el perfil deportivo. */
    private LocalDateTime createdAt;
    /** Fecha y hora de la última actualización del perfil deportivo. */
    private LocalDateTime updatedAt;
}