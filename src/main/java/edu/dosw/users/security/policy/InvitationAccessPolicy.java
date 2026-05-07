package edu.dosw.users.security.policy;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class InvitationAccessPolicy {
    // Policy helper for invitation resource ownership checks.
    // Example usages:
    // @PreAuthorize("hasAuthority('invitation:read:self') or @invitationAccessPolicy.canAccessOwnInvitation(#ownerId, authentication)")
    // @PreAuthorize("hasAuthority('invitation:respond:self') or @invitationAccessPolicy.canAccessOwnInvitation(#ownerId, authentication)")

    /**
     * Returns true when the authenticated principal represents the same user id
     * as the requested owner of the invitation. Follows the same conversion logic
     * as other policies in this project (principal -> String -> Long).
     */
    public boolean canAccessOwnInvitation(Long requestedOwnerId, Authentication authentication) {
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

