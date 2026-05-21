package edu.dosw.users.client;

/**
 * Interfaz de cliente para comunicarse con el microservicio de equipos.
 *
 * <p>Utilizada por los servicios de perfil deportivo e invitación para consultar
 * la pertenencia a equipos antes de realizar operaciones que están restringidas
 * mientras un jugador esté asignado a un equipo activo.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
public interface TeamsServiceClient {

    /**
     * Verifica si el usuario indicado está actualmente asignado a algún equipo.
     *
     * @param userId identificador del usuario a verificar
     * @return {@code true} si el usuario pertenece a al menos un equipo activo
     */
    boolean isPlayerAssignedToTeam(Long userId);
}