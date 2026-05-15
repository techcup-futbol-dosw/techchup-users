package edu.dosw.users.security.policy;

import edu.dosw.users.entity.SportProfileEntity;
import edu.dosw.users.repository.SportProfileRepository;
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
class SportProfileAccessPolicyTest {

    private SportProfileAccessPolicy policy;

    @Mock
    private Authentication authentication;

    @Mock
    private SportProfileRepository sportProfileRepository;

    @BeforeEach
    void setUp() {
        policy = new SportProfileAccessPolicy(sportProfileRepository);
    }

    // ── canAccessOwnSportProfile ──────────────────────────────────────────────

    @ParameterizedTest(name = "principal={0}, requestedId={1}")
    @CsvSource({
            "123, 123",
            "789, 789",
            "0, 0",
            "9223372036854775807, 9223372036854775807"
    })
    @DisplayName("Authenticated user can access own sport-profile for valid IDs")
    void testCanAccessOwnSportProfile_AuthorizedCases(String principal, Long requestedId) {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);

        boolean canAccess = policy.canAccessOwnSportProfile(requestedId, authentication);

        assertTrue(canAccess, "Matching authenticated principal should be allowed");
    }

    @ParameterizedTest(name = "requestedId={0}, isAuthenticated={1}, principal={2}, nullAuth={3}")
    @MethodSource("deniedAccessScenarios")
    @DisplayName("Access is denied for invalid or unauthorized scenarios")
    void testCanAccessOwnSportProfile_DeniedCases(Long requestedId,
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

        boolean canAccess = policy.canAccessOwnSportProfile(requestedId, authToUse);

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

    // ── canModifyOwnSportProfile ──────────────────────────────────────────────

    @Test
    @DisplayName("Owner can modify their own sport profile")
    void canModifyOwnSportProfile_ownerAllowed() {
        SportProfileEntity profile = SportProfileEntity.builder().id(10L).userId(42L).build();
        when(sportProfileRepository.findById(10L)).thenReturn(Optional.of(profile));
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("42");

        assertTrue(policy.canModifyOwnSportProfile(10L, authentication));
    }

    @Test
    @DisplayName("Non-owner cannot modify another user's sport profile")
    void canModifyOwnSportProfile_nonOwnerDenied() {
        SportProfileEntity profile = SportProfileEntity.builder().id(10L).userId(99L).build();
        when(sportProfileRepository.findById(10L)).thenReturn(Optional.of(profile));
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("42");

        assertFalse(policy.canModifyOwnSportProfile(10L, authentication));
    }

    @Test
    @DisplayName("Returns false when sport profile does not exist")
    void canModifyOwnSportProfile_profileNotFound_returnsFalse() {
        when(sportProfileRepository.findById(999L)).thenReturn(Optional.empty());
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("42");

        assertFalse(policy.canModifyOwnSportProfile(999L, authentication));
    }

    @Test
    @DisplayName("Returns false when authentication is null")
    void canModifyOwnSportProfile_nullAuth_returnsFalse() {
        assertFalse(policy.canModifyOwnSportProfile(10L, null));
        verifyNoInteractions(sportProfileRepository);
    }
}