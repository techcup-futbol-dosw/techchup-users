package edu.dosw.users.security.policy;

import edu.dosw.users.repository.InvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Política de acceso para operaciones sobre invitaciones de equipo.
 *
 * <p>Utilizada en expresiones {@code @PreAuthorize} de los controladores para determinar
 * si el principal autenticado puede consultar o responder a una invitación específica.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
@Component
@RequiredArgsConstructor
public class InvitationAccessPolicy extends AccessPolicySupport {

    private final InvitationRepository invitationRepository;

    /**
     * Retorna {@code true} cuando el principal autenticado es el propietario de la invitación
     * identificada por {@code requestedOwnerId} (la variable de ruta userId).
     *
     * @param requestedOwnerId identificador del usuario propietario solicitado
     * @param authentication   objeto de autenticación del contexto de seguridad
     * @return {@code true} si el principal coincide con el propietario solicitado
     */
    public boolean canAccessOwnInvitation(Long requestedOwnerId, Authentication authentication) {
        return matchesPrincipal(requestedOwnerId, authentication);
    }

    /**
     * Retorna {@code true} cuando el principal autenticado es el jugador que recibió
     * la invitación identificada por {@code invitationId} (consulta el userId en la base de datos).
     *
     * @param invitationId   identificador de la invitación a verificar
     * @param authentication objeto de autenticación del contexto de seguridad
     * @return {@code true} si el principal es el destinatario de la invitación
     */
    public boolean canRespondToInvitation(Long invitationId, Authentication authentication) {
        if (!isAuthenticatedUser(authentication)) {
            return false;
        }
        return invitationRepository.findById(invitationId)
                .map(invitation -> matchesPrincipal(invitation.getUserId(), authentication))
                .orElse(false);
    }
}