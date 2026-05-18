package edu.dosw.users.security.policy;

import org.springframework.security.core.Authentication;

/**
 * Shared helper methods for access-policy beans.
 *
 * <p>Centralises the principal-matching and authentication-check logic that
 * would otherwise be duplicated across every policy class.</p>
 */
abstract class AccessPolicySupport {

    /**
     * Returns {@code true} when the authenticated principal's ID equals
     * {@code userId}.
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
     * Returns {@code true} when {@code authentication} is non-null, marked as
     * authenticated, and carries a non-null principal.
     */
    protected boolean isAuthenticatedUser(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() != null;
    }
}