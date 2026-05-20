package edu.dosw.users.security.policy;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Política de acceso para operaciones sobre perfiles de usuario.
 *
 * <p>Utilizada en expresiones {@code @PreAuthorize} de los controladores para determinar
 * si el principal autenticado puede leer o modificar un perfil de usuario específico.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
@Component
public class UserAccessPolicy extends AccessPolicySupport {

    /**
     * Retorna {@code true} cuando el principal autenticado es el propietario
     * del perfil de usuario identificado por {@code requestedUserId}.
     *
     * @param requestedUserId identificador del usuario cuyo perfil se solicita
     * @param authentication  objeto de autenticación del contexto de seguridad
     * @return {@code true} si el principal coincide con el usuario solicitado
     */
    public boolean canAccessOwnUser(Long requestedUserId, Authentication authentication) {
        return matchesPrincipal(requestedUserId, authentication);
    }
}