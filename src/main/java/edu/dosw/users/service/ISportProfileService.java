package edu.dosw.users.service;

import edu.dosw.users.model.SportProfileModel;
import org.springframework.web.multipart.MultipartFile;

/**
 * Servicio para gestionar los perfiles deportivos de jugadores.
 *
 * <p>Un perfil deportivo no puede actualizarse mientras el jugador esté asignado
 * a un equipo activo, y nunca puede eliminarse. La gestión de fotos se delega
 * a {@link ImageService}.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
public interface ISportProfileService {

    /**
     * Recupera un perfil deportivo por su identificador.
     *
     * @param id identificador del perfil deportivo
     * @return modelo del perfil deportivo correspondiente
     * @throws edu.dosw.users.exception.ResourceNotFoundException si no se encuentra el perfil
     */
    SportProfileModel getById(Long id);

    /**
     * Recupera el perfil deportivo asociado al usuario indicado.
     *
     * @param userId identificador del usuario
     * @return modelo del perfil deportivo correspondiente
     * @throws edu.dosw.users.exception.ResourceNotFoundException si no se encuentra el perfil
     */
    SportProfileModel getByUserId(Long userId);

    /**
     * Crea un nuevo perfil deportivo para el usuario especificado.
     *
     * @param userId identificador del usuario propietario
     * @param model  datos del perfil deportivo
     * @param photo  foto del jugador (opcional); puede ser {@code null} o estar vacía
     * @return modelo guardado con su identificador generado
     * @throws edu.dosw.users.exception.ResourceNotFoundException si el usuario no existe
     * @throws edu.dosw.users.exception.BusinessException         si el usuario ya posee un perfil deportivo
     */
    SportProfileModel create(Long userId, SportProfileModel model, MultipartFile photo);

    /**
     * Actualiza un perfil deportivo existente.
     *
     * @param id    identificador del perfil deportivo a actualizar
     * @param model nuevos datos del perfil
     * @param photo nueva foto del jugador (opcional); {@code null} conserva la foto existente
     * @return modelo actualizado
     * @throws edu.dosw.users.exception.ResourceNotFoundException si no se encuentra el perfil
     * @throws edu.dosw.users.exception.BusinessException         si el jugador está actualmente asignado a un equipo
     */
    SportProfileModel update(Long id, SportProfileModel model, MultipartFile photo);

    /**
     * Actualiza el indicador de disponibilidad del perfil deportivo indicado.
     *
     * @param id        identificador del perfil deportivo
     * @param available nuevo valor de disponibilidad
     * @throws edu.dosw.users.exception.ResourceNotFoundException si no se encuentra el perfil
     */
    void updateAvailability(Long id, boolean available);
}