package edu.dosw.users.security.policy;

import edu.dosw.users.repository.SportProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Política de acceso para operaciones sobre perfiles deportivos.
 *
 * <p>Utilizada en expresiones {@code @PreAuthorize} de los controladores para determinar
 * si el principal autenticado puede leer o modificar un perfil deportivo específico.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class SportProfileAccessPolicy extends AccessPolicySupport {

    private final SportProfileRepository sportProfileRepository;

    /**
     * Retorna {@code true} cuando el principal autenticado es el propietario del perfil deportivo
     * identificado por {@code requestedOwnerId} (la variable de ruta userId).
     *
     * @param requestedOwnerId identificador del usuario propietario solicitado
     * @param authentication   objeto de autenticación del contexto de seguridad
     * @return {@code true} si el principal coincide con el propietario solicitado
     */
    public boolean canAccessOwnSportProfile(Long requestedOwnerId, Authentication authentication) {
        return matchesPrincipal(requestedOwnerId, authentication);
    }

    /**
     * Retorna {@code true} cuando el principal autenticado es el propietario del perfil deportivo
     * identificado por su {@code id} de perfil (consulta el userId propietario en la base de datos).
     *
     * @param profileId      identificador del perfil deportivo a verificar
     * @param authentication objeto de autenticación del contexto de seguridad
     * @return {@code true} si el principal es el propietario del perfil deportivo
     */
    public boolean canModifyOwnSportProfile(Long profileId, Authentication authentication) {
        if (!isAuthenticatedUser(authentication)) {
            return false;
        }
        return sportProfileRepository.findById(profileId)
                .map(profile -> matchesPrincipal(profile.getUserId(), authentication))
                .orElse(false);
    }
}