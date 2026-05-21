package edu.dosw.users.security.policy;

import org.springframework.security.core.Authentication;

/**
 * Métodos de ayuda compartidos para los beans de política de acceso.
 *
 * <p>Centraliza la lógica de coincidencia de principal y verificación de autenticación
 * que de otro modo se duplicaría en cada clase de política.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
abstract class AccessPolicySupport {

    /**
     * Retorna {@code true} cuando el ID del principal autenticado es igual a {@code userId}.
     *
     * @param userId         identificador del usuario a comparar con el principal actual
     * @param authentication objeto de autenticación del contexto de seguridad
     * @return {@code true} si el principal coincide con el userId indicado
     */
    protected boolean matchesPrincipal(Long userId, Authentication authentication) {
        if (!isAuthenticatedUser(authentication)) {
            return false;
        }
        try {
            Long currentUserId = Long.valueOf(authentication.getPrincipal().toString());
            return userId.equals(currentUserId);
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    /**
     * Retorna {@code true} cuando {@code authentication} no es nulo, está marcado como
     * autenticado y lleva un principal no nulo.
     *
     * @param authentication objeto de autenticación del contexto de seguridad
     * @return {@code true} si el usuario está correctamente autenticado
     */
    protected boolean isAuthenticatedUser(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() != null;
    }
}