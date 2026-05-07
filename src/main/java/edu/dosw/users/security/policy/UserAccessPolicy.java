package edu.dosw.users.security.policy;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class UserAccessPolicy {
    // Se debe crear una policy para cada recurso/servicio que necesite validar
    // si el dueño del recurso puede acceder o no.

    // Ejemplo de uso: el propietario del usuario puede leer su propio usuario
    // @PreAuthorize("hasAuthority('user:read:any') or @userAccessPolicy.canAccessOwnUser(#userId, authentication)")

    // Se debe cambiar el nombre de la clase al nombre del recurso asociado.

    //Ejemplo para capitan dueño del equipo
    // @PreAuthorize("hasAuthority('team:update:any') or @teamAccessPolicy.canUpdateTeam(#teamId, authentication)")
    // @PostMapping("/{id}")
    // public ResponseEntity<ExampleResponse> updateTeam(@PathVariable Long teamId){....

    public boolean canAccessOwnUser(Long requestedUserId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (principal == null) {
            return false;
        }

        try {
            Long currentUserId = Long.valueOf(principal.toString());
            return requestedUserId.equals(currentUserId);
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}


