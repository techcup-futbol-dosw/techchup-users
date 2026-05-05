package edu.dosw.users.security.policy;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class ResourceAccessPolicy {
    // Se debe crear un ResourceAccessPolicy para cada endpoint de un servicio que
    // se requiera saber si el dueño del recurso puede acceder o no

    //Ejemplo de uso: el propietario de la cuenta puede leer su propia cuenta
    //@PreAuthorize("hasAuthority('account:read:any') or @resourceAccessPolicy.canAccessOwnResource(#accountId, authentication)")

    //Se debe cambiar el nombre de la clase Resource al nombre del recurso asociado

    //Ejemplo para capitan dueño del equipo
    //@PreAuthorize("hasAuthority('team:update:any') or @teamAccessPolicy.canUpdateTeam(#teamId, authentication)")
    //@PostMapping("/{id}")
    //public ResponseEntity<ExampleResponse> updateTeam(@PathVariable Long #teamId){....

    public boolean canAccessOwnResource(Long requestedAccountId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Long currentAccountId = Long.valueOf(authentication.getPrincipal().toString());
        return requestedAccountId.equals(currentAccountId);
    }
}

