package edu.dosw.users.repository;

import edu.dosw.users.entity.SportProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para operaciones de persistencia de {@link SportProfileEntity}.
 *
 * <p>Provee las operaciones CRUD estándar heredadas de {@link JpaRepository} y
 * consultas derivadas para recuperar perfiles deportivos por identificador de usuario,
 * posición y disponibilidad.</p>
 *
 * @author CodeForge
 * @since 1.0
 * @see SportProfileEntity
 */
public interface SportProfileRepository extends JpaRepository<SportProfileEntity, Long> {

    /**
     * Busca el perfil deportivo asociado al usuario indicado.
     *
     * @param userId identificador del usuario propietario del perfil deportivo
     * @return optional con el perfil deportivo si existe; vacío si no hay perfil para ese usuario
     */
    Optional<SportProfileEntity> findByUserId(Long userId);

    /**
     * Busca todos los perfiles deportivos con la posición indicada.
     *
     * @param position cadena de posición por la que filtrar (p. ej. {@code "FORWARD"})
     * @return lista de perfiles deportivos con la posición solicitada
     */
    List<SportProfileEntity> findByPosition(String position);

    /**
     * Busca todos los perfiles deportivos que coincidan con el indicador de disponibilidad dado.
     *
     * @param available {@code true} para retornar solo jugadores disponibles
     * @return lista de perfiles deportivos con la disponibilidad solicitada
     */
    List<SportProfileEntity> findByAvailable(boolean available);

    /**
     * Busca todos los perfiles deportivos que coincidan con la posición y la disponibilidad indicadas.
     *
     * @param position  cadena de posición por la que filtrar
     * @param available indicador de disponibilidad por el que filtrar
     * @return lista de perfiles deportivos que cumplen ambos criterios
     */
    List<SportProfileEntity> findByPositionAndAvailable(String position, boolean available);

    /**
     * Finds all sport profiles whose owner is in the given set of user identifiers.
     * Used to batch-load sport profiles after a user search.
     *
     * @param userIds set of user identifiers to look up
     * @return list of sport profiles for the given users
     */
    List<SportProfileEntity> findByUserIdIn(java.util.Collection<Long> userIds);
}
