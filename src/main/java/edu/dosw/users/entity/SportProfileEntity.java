package edu.dosw.users.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidad JPA que representa el perfil deportivo de un jugador.
 *
 * <p>Mapeada a la tabla {@code sport_profiles}. El campo {@code userId} almacena
 * el identificador del propietario, gestionado por el servicio de identidad.
 * El campo {@code photoId} almacena el {@code ObjectId} del documento
 * {@code PlayerPhoto} en MongoDB.</p>
 *
 * @author CodeForge
 * @since 1.0
 * @see edu.dosw.users.model.SportProfileModel
 * @see edu.dosw.users.mapper.SportProfileMapper
 */
@Entity
@Table(name = "sport_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SportProfileEntity {

    /** Identificador único generado automáticamente por la base de datos. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Posición táctica del jugador almacenada como {@code String} (p. ej. {@code "GOALKEEPER"}). */
    @Column(name = "position", nullable = false)
    private String position;

    /** Número de dorsal del jugador. */
    @Column(name = "dorsal_number")
    private Integer dorsalNumber;

    /** ObjectId del documento de foto en MongoDB (24 caracteres hexadecimales). */
    @Column(name = "photo_id", length = 24)
    private String photoId;

    /** Indica si el jugador está disponible para participar en torneos. */
    @Column(name = "available", nullable = false)
    private boolean available;

    /** Fecha y hora en que se creó el perfil deportivo. */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /** Fecha y hora de la última actualización del perfil deportivo. */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Identificador del usuario propietario de este perfil deportivo.
     * Gestionado por el servicio de identidad; almacenado como columna simple sin relación JPA.
     */
    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;
}
