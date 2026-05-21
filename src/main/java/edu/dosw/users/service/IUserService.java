package edu.dosw.users.service;

import edu.dosw.users.dto.PlayerSearchResponse;
import edu.dosw.users.model.UserModel;

import java.util.List;

/**
 * Servicio para gestionar los perfiles de usuario.
 *
 * <p>Maneja las operaciones CRUD sobre {@link UserModel}. La desactivación establece
 * el estado del perfil a {@code "INACTIVE"} sin eliminar datos de la base de datos.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
public interface IUserService {

    /**
     * Recupera un perfil de usuario por su identificador.
     *
     * @param id identificador del perfil
     * @return modelo del usuario correspondiente
     * @throws edu.dosw.users.exception.ResourceNotFoundException si no se encuentra el usuario
     */
    UserModel getById(Long id);

    /**
     * Recupera un perfil de usuario por su número de identificación oficial.
     *
     * @param identification número de identificación oficial
     * @return modelo del usuario correspondiente
     * @throws edu.dosw.users.exception.ResourceNotFoundException si no se encuentra el usuario
     */
    UserModel getByIdentification(String identification);

    /**
     * Retorna todos los perfiles de usuario almacenados en el sistema.
     *
     * @return lista de todos los modelos de usuario; puede estar vacía
     */
    List<UserModel> getAll();

    /**
     * Actualiza un perfil de usuario existente con los datos proporcionados.
     *
     * @param id    identificador del perfil a actualizar
     * @param model nuevos datos del perfil
     * @return modelo del usuario actualizado
     * @throws edu.dosw.users.exception.ResourceNotFoundException si no se encuentra el usuario
     */
    UserModel update(Long id, UserModel model);

    /**
     * Actualiza el perfil del usuario actual sin modificar su correo electrónico ni contraseña.
     *
     * @param userId identificador del usuario actual
     * @param model  nuevos datos del perfil
     * @return modelo del perfil actualizado
     * @throws edu.dosw.users.exception.ResourceNotFoundException si no se encuentra el usuario
     */
    UserModel updateProfile(Long userId, UserModel model);

    /**
     * Desactiva un perfil de usuario estableciendo su estado a {@code "INACTIVE"}.
     *
     * @param id identificador del perfil a desactivar
     * @throws edu.dosw.users.exception.ResourceNotFoundException si no se encuentra el usuario
     */
    void deactivate(Long id);

    /**
     * Inactiva un perfil de usuario tras validar su participación en un torneo.
     *
     * @param id identificador del perfil a inactivar
     * @throws edu.dosw.users.exception.ResourceNotFoundException si no se encuentra el usuario
     * @throws edu.dosw.users.exception.BusinessException         si el usuario pertenece a un equipo activo en un torneo
     */
    void inactivate(Long id);

    /**
     * Reactiva un perfil de usuario estableciendo su estado a {@code ACTIVE}.
     *
     * @param id identificador del perfil a reactivar
     * @throws edu.dosw.users.exception.ResourceNotFoundException si no se encuentra el usuario
     * @throws edu.dosw.users.exception.BusinessException         si la cuenta ya está activa
     */
    void reactivate(Long id);

    /**
     * Busca jugadores que coincidan con los filtros indicados.
     *
     * <p>Los filtros de nombre y estado se delegan al servicio de identidad; los
     * restantes se aplican localmente usando datos del perfil deportivo y del modelo de usuario.</p>
     *
     * @param name           filtro parcial de nombre (insensible a mayúsculas, opcional)
     * @param position       filtro de posición, p. ej. {@code "FORWARD"} (opcional)
     * @param status         filtro de estado, p. ej. {@code "ACTIVE"} (opcional)
     * @param identification filtro de número de identificación exacto (opcional)
     * @param gender         filtro de género (opcional)
     * @param semester       filtro de semestre exacto (opcional)
     * @param age            filtro de edad exacta calculada desde la fecha de nacimiento (opcional)
     * @param onlyAvailable  cuando es {@code true}, retorna solo jugadores con perfil deportivo disponible
     * @return lista de modelos de usuario coincidentes; puede estar vacía
     */
    List<UserModel> search(String name, String position, String status,
                           String identification, String gender,
                           Integer semester, Integer age, Boolean onlyAvailable);

    /**
     * Searches for players and enriches each result with sport profile data.
     *
     * <p>Delegates filtering to {@link #search} and then batch-loads sport profiles
     * so that {@code position}, {@code dorsalNumber} and {@code available} are
     * included in every result that has a sport profile.</p>
     *
     * @param name           optional partial name filter
     * @param position       optional position filter (e.g. {@code "FORWARD"})
     * @param status         optional status filter
     * @param identification optional exact identification number filter
     * @param gender         optional gender filter
     * @param semester       optional exact semester filter
     * @param age            optional exact age filter
     * @param onlyAvailable  when {@code true}, only returns available players
     * @return list of enriched player search responses, may be empty
     */
    List<PlayerSearchResponse> searchPlayers(String name, String position, String status,
                                             String identification, String gender,
                                             Integer semester, Integer age, Boolean onlyAvailable);
}