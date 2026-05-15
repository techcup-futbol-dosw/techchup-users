package edu.dosw.users.security.policy;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class UserAccessPolicy extends AccessPolicySupport {

    public boolean canAccessOwnUser(Long requestedUserId, Authentication authentication) {
        return matchesPrincipal(requestedUserId, authentication);
    }
}