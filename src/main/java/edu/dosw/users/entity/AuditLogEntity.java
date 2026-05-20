package edu.dosw.users.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa una entrada del log de auditoría en la base de datos relacional.
 *
 * <p>Mapeada a la tabla {@code audit_logs}. La restricción {@code chk_audit_log_has_reference}
 * impone a nivel de base de datos que al menos una de las relaciones ({@code sportProfile} o
 * {@code invitation}) sea no nula, garantizando la trazabilidad de cada evento auditado.</p>
 *
 * @author CodeForge
 * @since 1.0
 * @see edu.dosw.users.model.AuditLogModel
 * @see edu.dosw.users.mapper.AuditLogMapper
 */
@Entity
@Table(
    name = "audit_logs",
    check = @CheckConstraint(
        name = "chk_audit_log_has_reference",
        constraint = "sport_profile_id IS NOT NULL OR invitation_id IS NOT NULL"
    )
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntity {

    /** Identificador único generado automáticamente por la base de datos. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tipo de acción auditada almacenado como {@code String} (p. ej. {@code "CREATE"}). */
    @Column(name = "action", nullable = false)
    private String action;

    /** Fecha y hora exacta en que ocurrió el evento auditado. */
    @Column(name = "action_timestamp", nullable = false)
    private LocalDateTime timestamp;

    /** Descripción adicional o contexto del evento auditado. */
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    /** Perfil deportivo relacionado con el evento; puede ser {@code null} si el evento concierne a una invitación. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sport_profile_id")
    private SportProfileEntity sportProfile;

    /** Invitación relacionada con el evento; puede ser {@code null} si el evento concierne a un perfil deportivo. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitation_id")
    private InvitationEntity invitation;
}