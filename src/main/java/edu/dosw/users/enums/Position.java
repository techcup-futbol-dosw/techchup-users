package edu.dosw.users.enums;

/**
 * Posiciones de juego disponibles para un jugador de fútbol.
 *
 * <p>Se usa en {@code SportProfileModel} y {@code SportProfileEntity}
 * para clasificar el rol táctico del jugador en el campo.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
public enum Position {
    /** Portero — responsable de defender la portería del equipo. */
    GOALKEEPER,
    /** Defensa — posición defensiva cercana a la portería propia. */
    DEFENDER,
    /** Centrocampista — enlace entre la defensa y el ataque. */
    MIDFIELDER,
    /** Delantero — posición ofensiva encargada de marcar goles. */
    FORWARD
}