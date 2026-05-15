package edu.dosw.users.security.policy;

import edu.dosw.users.entity.InvitationEntity;
import edu.dosw.users.repository.InvitationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvitationAccessPolicyTest {

    private InvitationAccessPolicy policy;

    @Mock
    private Authentication authentication;

    @Mock
    private InvitationRepository invitationRepository;

    @BeforeEach
    void setUp() {
        policy = new InvitationAccessPolicy(invitationRepository);
    }

    // ── canAccessOwnInvitation ────────────────────────────────────────────────

    @ParameterizedTest(name = "principal={0}, requestedId={1}")
    @CsvSource({
            "123, 123",
            "789, 789",
            "0, 0",
            "9223372036854775807, 9223372036854775807"
    })
    @DisplayName("Authenticated user can access own invitations for valid IDs")
    void testCanAccessOwnInvitation_AuthorizedCases(String principal, Long requestedId) {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);

        boolean canAccess = policy.canAccessOwnInvitation(requestedId, authentication);

        assertTrue(canAccess, "Matching authenticated principal should be allowed");
    }

    @ParameterizedTest(name = "requestedId={0}, isAuthenticated={1}, principal={2}, nullAuth={3}")
    @MethodSource("deniedAccessScenarios")
    @DisplayName("Access is denied for invalid or unauthorized scenarios")
    void testCanAccessOwnInvitation_DeniedCases(Long requestedId,
                                                  Boolean isAuthenticated,
                                                  String principal,
                                                  boolean useNullAuthentication) {
        Authentication authToUse = null;
        if (!useNullAuthentication) {
            when(authentication.isAuthenticated()).thenReturn(isAuthenticated);
            if (Boolean.TRUE.equals(isAuthenticated)) {
                when(authentication.getPrincipal()).thenReturn(principal);
            }
            authToUse = authentication;
        }

        boolean canAccess = policy.canAccessOwnInvitation(requestedId, authToUse);

        assertFalse(canAccess, "Invalid or unauthorized context must be denied (fail-closed)");
    }

    private static Stream<Arguments> deniedAccessScenarios() {
        return Stream.of(
                Arguments.of(456L, true, "123", false),
                Arguments.of(123L, false, "123", false),
                Arguments.of(123L, true, " 123 ", false),
                Arguments.of(123L, true, null, false),
                Arguments.of(123L, null, null, true)
        );
    }

    // ── canRespondToInvitation ────────────────────────────────────────────────

    @Test
    @DisplayName("Recipient can respond to their own invitation")
    void canRespondToInvitation_recipientAllowed() {
        InvitationEntity invitation = InvitationEntity.builder().id(5L).userId(42L).build();
        when(invitationRepository.findById(5L)).thenReturn(Optional.of(invitation));
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("42");

        assertTrue(policy.canRespondToInvitation(5L, authentication));
    }

    @Test
    @DisplayName("Non-recipient cannot respond to another user's invitation")
    void canRespondToInvitation_nonRecipientDenied() {
        InvitationEntity invitation = InvitationEntity.builder().id(5L).userId(99L).build();
        when(invitationRepository.findById(5L)).thenReturn(Optional.of(invitation));
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("42");

        assertFalse(policy.canRespondToInvitation(5L, authentication));
    }

    @Test
    @DisplayName("Returns false when invitation does not exist")
    void canRespondToInvitation_invitationNotFound_returnsFalse() {
        when(invitationRepository.findById(999L)).thenReturn(Optional.empty());
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("42");

        assertFalse(policy.canRespondToInvitation(999L, authentication));
    }

    @Test
    @DisplayName("Returns false when authentication is null")
    void canRespondToInvitation_nullAuth_returnsFalse() {
        assertFalse(policy.canRespondToInvitation(5L, null));
        verifyNoInteractions(invitationRepository);
    }
}