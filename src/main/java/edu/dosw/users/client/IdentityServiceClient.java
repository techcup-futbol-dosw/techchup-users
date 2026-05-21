package edu.dosw.users.client;

import edu.dosw.users.model.UserModel;

import java.util.List;

/**
 * Interfaz de cliente para comunicarse con el microservicio de identidad.
 *
 * <p>Utilizada por la capa de servicio para delegar todas las operaciones de datos de usuario
 * (CRUD, desactivación, inactivación y búsqueda) al servicio de identidad en lugar de
 * persistir los datos de usuario localmente.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
public interface IdentityServiceClient {

    /**
     * Retorna {@code true} cuando existe un usuario con el identificador dado en el servicio de identidad.
     *
     * @param id identificador de usuario a verificar
     * @return {@code true} si el usuario existe
     */
    boolean userExists(Long id);

    /**
     * Recupera un usuario por su identificador.
     *
     * @param id identificador del usuario
     * @return modelo del usuario, o {@code null} si no se encuentra
     */
    UserModel getUserById(Long id);

    /**
     * Recupera un usuario por su número de identificación oficial.
     *
     * @param identification número de identificación oficial
     * @return modelo del usuario, o {@code null} si no se encuentra
     */
    UserModel getUserByIdentification(String identification);

    /**
     * Retorna todos los usuarios registrados en el servicio de identidad.
     *
     * @return lista de todos los modelos de usuario; puede estar vacía
     */
    List<UserModel> getAllUsers();

    /**
     * Crea un nuevo usuario en el servicio de identidad.
     *
     * @param model datos del usuario a crear
     * @return modelo del usuario creado con su identificador generado
     */
    UserModel createUser(UserModel model);

    /**
     * Reemplaza los datos de un usuario existente en el servicio de identidad.
     *
     * @param id    identificador del usuario a actualizar
     * @param model nuevos datos del usuario
     * @return modelo del usuario actualizado
     */
    UserModel updateUser(Long id, UserModel model);

    /**
     * Actualiza el perfil propio del usuario actual en el servicio de identidad.
     *
     * @param userId identificador del usuario autenticado
     * @param model  nuevos datos del perfil (sin credenciales)
     * @return modelo del usuario actualizado
     */
    UserModel updateUserProfile(Long userId, UserModel model);

    /**
     * Establece el estado de un usuario a {@code INACTIVE} en el servicio de identidad.
     *
     * @param id identificador del usuario a desactivar
     */
    void deactivateUser(Long id);

    /**
     * Inactiva un usuario en el servicio de identidad.
     *
     * @param id identificador del usuario a inactivar
     */
    void inactivateUser(Long id);

    /**
     * Reactiva un usuario en el servicio de identidad estableciendo su estado a {@code ACTIVE}.
     *
     * @param id identificador del usuario a reactivar
     */
    void reactivateUser(Long id);

    /**
     * Busca usuarios en el servicio de identidad que coincidan con los filtros indicados.
     *
     * @param name   filtro parcial de nombre (insensible a mayúsculas, opcional); {@code null} retorna todos
     * @param status filtro de estado (p. ej. {@code "ACTIVE"}, opcional); {@code null} retorna todos
     * @return lista de modelos de usuario coincidentes; puede estar vacía
     */
    List<UserModel> searchUsers(String name, String status);
}
