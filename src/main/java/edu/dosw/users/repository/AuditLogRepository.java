package edu.dosw.users.repository;

import edu.dosw.users.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para operaciones de persistencia de {@link AuditLogEntity}.
 *
 * <p>Provee las operaciones CRUD estándar heredadas de {@link JpaRepository} y
 * consultas derivadas para recuperar entradas de auditoría asociadas a perfiles
 * deportivos o invitaciones.</p>
 *
 * @author CodeForge
 * @since 1.0
 * @see AuditLogEntity
 */
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {

    /**
     * Busca todas las entradas de auditoría vinculadas al perfil deportivo indicado.
     *
     * @param sportProfileId identificador del perfil deportivo cuyas entradas de auditoría se solicitan
     * @return lista de entradas de auditoría asociadas al perfil deportivo
     */
    List<AuditLogEntity> findBySportProfile_Id(Long sportProfileId);

    /**
     * Busca todas las entradas de auditoría vinculadas a la invitación indicada.
     *
     * @param invitationId identificador de la invitación cuyas entradas de auditoría se solicitan
     * @return lista de entradas de auditoría asociadas a la invitación
     */
    List<AuditLogEntity> findByInvitation_Id(Long invitationId);
}
