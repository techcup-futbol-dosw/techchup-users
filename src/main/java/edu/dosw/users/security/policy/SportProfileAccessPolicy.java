package edu.dosw.users.security.policy;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class SportProfileAccessPolicy {
    // Policy helper for sport-profile resource ownership checks.
    // Example usage in a controller or service level expression:
    // @PreAuthorize("hasAuthority('sport-profile:read:any') or @sportProfileAccessPolicy.canAccessOwnSportProfile(#ownerId, authentication)")

    /**
     * Returns true when the authenticated principal represents the same user id
     * as the requested owner of the sport profile. The project stores the user id
     * as the Authentication.principal (stringifiable), so this method follows the
     * same conversion logic as other policies.
     */
    public boolean canAccessOwnSportProfile(Long requestedOwnerId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (principal == null) {
            return false;
        }

        try {
            Long currentUserId = Long.valueOf(principal.toString());
            return requestedOwnerId.equals(currentUserId);
        } catch (NumberFormatException ex) {
            return false;
        }
    }
}

