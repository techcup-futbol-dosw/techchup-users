package edu.dosw.users.security.policy;

import edu.dosw.users.repository.InvitationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InvitationAccessPolicy extends AccessPolicySupport {

    private final InvitationRepository invitationRepository;

    /**
     * Returns true when the authenticated principal owns the invitation
     * identified by {@code requestedOwnerId} (the userId path variable).
     */
    public boolean canAccessOwnInvitation(Long requestedOwnerId, Authentication authentication) {
        return matchesPrincipal(requestedOwnerId, authentication);
    }

    /**
     * Returns true when the authenticated principal is the player who received
     * the invitation identified by {@code invitationId} (looks up the userId in the DB).
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