package edu.dosw.users.security.policy;

import edu.dosw.users.repository.SportProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SportProfileAccessPolicy {

    private final SportProfileRepository sportProfileRepository;

    /**
     * Returns true when the authenticated principal owns the sport profile
     * identified by {@code requestedOwnerId} (the userId path variable).
     */
    public boolean canAccessOwnSportProfile(Long requestedOwnerId, Authentication authentication) {
        return matchesPrincipal(requestedOwnerId, authentication);
    }

    /**
     * Returns true when the authenticated principal owns the sport profile
     * identified by its profile {@code id} (looks up the owning userId in the DB).
     */
    public boolean canModifyOwnSportProfile(Long profileId, Authentication authentication) {
        if (!isAuthenticatedUser(authentication)) {
            return false;
        }
        return sportProfileRepository.findById(profileId)
                .map(profile -> matchesPrincipal(profile.getUserId(), authentication))
                .orElse(false);
    }

    private boolean matchesPrincipal(Long userId, Authentication authentication) {
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

    private boolean isAuthenticatedUser(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() != null;
    }
}