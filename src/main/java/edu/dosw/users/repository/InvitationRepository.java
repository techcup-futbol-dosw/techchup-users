package edu.dosw.users.repository;

import edu.dosw.users.entity.InvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio Spring Data JPA para operaciones de persistencia de {@link InvitationEntity}.
 *
 * <p>Incluye las operaciones CRUD estándar heredadas de {@link JpaRepository} y
 * consultas derivadas para recuperar invitaciones por identificador de usuario y estado.</p>
 *
 * @author CodeForge
 * @since 1.0
 * @see InvitationEntity
 */
public interface InvitationRepository extends JpaRepository<InvitationEntity, Long> {

    /**
     * Busca todas las invitaciones recibidas por el usuario indicado.
     *
     * @param userId identificador del usuario cuyas invitaciones se solicitan
     * @return lista de invitaciones asociadas al usuario
     */
    List<InvitationEntity> findByUserId(Long userId);

    /**
     * Busca todas las invitaciones recibidas por el usuario indicado que coincidan con el estado solicitado.
     *
     * @param userId identificador del usuario cuyas invitaciones se solicitan
     * @param status estado de la invitación por el que filtrar
     * @return lista de invitaciones asociadas al usuario con el estado indicado
     */
    List<InvitationEntity> findByUserIdAndStatus(Long userId, String status);
}
