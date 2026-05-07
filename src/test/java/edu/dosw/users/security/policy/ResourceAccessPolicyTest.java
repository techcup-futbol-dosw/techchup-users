package edu.dosw.users.security.policy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for ResourceAccessPolicy.
 *
 * <p>This test class validates the resource-level access control policy that determines
 * whether an authenticated user can access a specific resource. The policy implements
 * the principle of "own resource access" - users can access resources they own but
 * cannot access resources owned by other users.
 *
 * <p><b>Test Coverage</b>:
 * <ul>
 *   <li><b>Own Resource Access</b>: Validates that users can access their own resources</li>
 *   <li><b>Cross-User Protection</b>: Denies access to other users' resources</li>
 *   <li><b>Unauthenticated Access</b>: Denies access for null or non-authenticated requests</li>
 *   <li><b>Edge Cases</b>: Large IDs, zero IDs, whitespace, null principals</li>
 *   <li><b>Type Conversion</b>: String-to-Long conversion for principal IDs</li>
 * </ul>
 *
 * <p><b>Approach</b>:
 * Uses Mockito to mock Spring Security Authentication objects. Tests verify the policy
 * decision logic for authorized/denied access in various scenarios. Each test uses the
 * Arrange-Act-Assert pattern for clarity.
 *
 * <p><b>Security Implications</b>:
 * This policy is critical for preventing unauthorized access to user-owned resources.
 * Tests ensure that the policy cannot be bypassed through edge cases or type mismatches.
 *
 * @see ResourceAccessPolicy
 */
@ExtendWith(MockitoExtension.class)
class ResourceAccessPolicyTest {

    private ResourceAccessPolicy policy;

    @Mock
    private Authentication authentication;

    /**
     * Setup method executed before each test case.
     * Initializes a fresh ResourceAccessPolicy instance to ensure test isolation
     * and prevent state carryover between test cases.
     */
    @BeforeEach
    void setUp() {
        // Initialize policy for testing
        policy = new ResourceAccessPolicy();
    }

    // ========== Own Resource Access Tests ==========

    @ParameterizedTest(name = "principal={0}, requestedId={1}")
    @CsvSource({
            "123, 123",
            "789, 789",
            "0, 0",
            "9223372036854775807, 9223372036854775807"
    })
    @DisplayName("Authenticated user can access own resource for valid IDs")
    void testCanAccessOwnResource_AuthorizedCases(String principal, Long requestedId) {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(principal);

        boolean canAccess = policy.canAccessOwnResource(requestedId, authentication);

        assertTrue(canAccess, "Matching authenticated principal should be allowed");
    }

    @ParameterizedTest(name = "requestedId={0}, isAuthenticated={1}, principal={2}, nullAuth={3}")
    @MethodSource("deniedAccessScenarios")
    @DisplayName("Access is denied for invalid or unauthorized scenarios")
    void testCanAccessOwnResource_DeniedCases(Long requestedId,
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

        boolean canAccess = policy.canAccessOwnResource(requestedId, authToUse);

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
}

