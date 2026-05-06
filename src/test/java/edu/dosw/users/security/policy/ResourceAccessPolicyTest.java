package edu.dosw.users.security.policy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
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

    /**
     * Validates that an authenticated user can access a resource they own.
     * 
     * <p><b>Scenario</b>: A user with principal ID "123" requests access to resource ID 123L.
     * Both IDs match, indicating the user owns the resource.
     * 
     * <p><b>Expected Behavior</b>: The policy returns {@code true} granting access to the own resource.
     * 
     * <p><b>Security Importance</b>: This is the happy path - users must be able to access
     * their own resources without restriction.
     */
    @Test
    @DisplayName("Authenticated user can access own resource")
    void testCanAccessOwnResource_UserAccessingOwnResource() {
        // Arrange: Set up mocked authenticated user with principal ID "123"
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("123");

        // Act: Request access to resource with ID 123L (user's own resource)
        boolean canAccess = policy.canAccessOwnResource(123L, authentication);

        // Assert: Verify access is granted
        assertTrue(canAccess, "User should be able to access their own resource");
    }

    /**
     * Validates that users cannot access resources owned by other users.
     * 
     * <p><b>Scenario</b>: A user with principal ID "123" requests access to resource ID 456L.
     * IDs do not match, indicating the resource belongs to another user.
     * 
     * <p><b>Expected Behavior</b>: The policy returns {@code false} denying access.
     * 
     * <p><b>Security Importance</b>: This is a critical security check preventing
     * unauthorized access to other users' data (multi-tenancy boundary).
     */
    @Test
    @DisplayName("User cannot access another user's resource")
    void testCanAccessOwnResource_UserAccessingOtherUserResource() {
        // Arrange: Set up mocked authenticated user with principal ID "123"
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("123");

        // Act: Request access to resource with ID 456L (different user's resource)
        boolean canAccess = policy.canAccessOwnResource(456L, authentication);

        // Assert: Verify access is denied
        assertFalse(canAccess, "User should not be able to access another user's resource");
    }

    // ========== Unauthenticated Access Tests ==========

    /**
     * Validates that null authentication (no user) denies all resource access.
     * 
     * <p><b>Scenario</b>: A request is made without any authentication (authentication parameter is null).
     * This would occur for unauthenticated/anonymous requests.
     * 
     * <p><b>Expected Behavior</b>: The policy returns {@code false} denying access.
     * 
     * <p><b>Security Importance</b>: Null checks are essential to prevent NullPointerException
     * and ensure unauthenticated users cannot bypass access control.
     */
    @Test
    @DisplayName("Null authentication denies access")
    void testCanAccessOwnResource_NullAuthentication() {
        // Act: Request access with null authentication (no user)
        boolean canAccess = policy.canAccessOwnResource(123L, null);

        // Assert: Verify access is denied
        assertFalse(canAccess, "Null authentication should deny access");
    }

    /**
     * Validates that non-authenticated Authentication objects deny access.
     * 
     * <p><b>Scenario</b>: An Authentication object exists but isAuthenticated() returns false.
     * This could indicate a partially-constructed or session-expired authentication.
     * 
     * <p><b>Expected Behavior</b>: The policy returns {@code false} denying access.
     * 
     * <p><b>Security Importance</b>: Ensures that only fully-authenticated users can access resources.
     */
    @Test
    @DisplayName("Non-authenticated user denies access")
    void testCanAccessOwnResource_NonAuthenticatedUser() {
        // Arrange: Set up Authentication object that is not authenticated
        when(authentication.isAuthenticated()).thenReturn(false);

        // Act: Request access (authentication exists but is not authenticated)
        boolean canAccess = policy.canAccessOwnResource(123L, authentication);

        // Assert: Verify access is denied
        assertFalse(canAccess, "Non-authenticated user should be denied access");
    }

    // ========== Edge Cases Tests ==========

    /**
     * Validates that string-formatted principal IDs are correctly converted to Long.
     * 
     * <p><b>Scenario</b>: The authentication principal is stored as a String (standard in Spring Security),
     * and needs to be converted to Long for comparison with the resource ID.
     * 
     * <p><b>Expected Behavior</b>: The policy correctly parses the string and grants access
     * when IDs match (string "789" converts to Long 789L).
     * 
     * <p><b>Implementation Detail</b>: Tests the String-to-Long conversion logic that is
     * essential for the policy to work with Spring Security's standard principal format.
     */
    @Test
    @DisplayName("String principal ID is correctly converted to Long")
    void testCanAccessOwnResource_StringPrincipal() {
        // Arrange: Set up authenticated user with principal as String (common in Spring Security)
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("789");

        // Act: Request access to resource 789L (ID as Long)
        boolean canAccess = policy.canAccessOwnResource(789L, authentication);

        // Assert: Verify string-to-Long conversion works and access is granted
        assertTrue(canAccess, "String principal should be correctly converted to Long");
    }

    /**
     * Validates that zero (0) is a valid ID and is correctly compared.
     * 
     * <p><b>Scenario</b>: While unlikely in production, testing ID 0 ensures the policy
     * doesn't treat 0 as a falsy value that should be rejected.
     * 
     * <p><b>Expected Behavior</b>: The policy grants access when principal and resource ID are both 0.
     * 
     * <p><b>Robustness</b>: Ensures the equality check uses proper Long comparison, not truthiness checks.
     */
    @Test
    @DisplayName("Zero ID is correctly compared")
    void testCanAccessOwnResource_ZeroId() {
        // Arrange: Set up authenticated user with principal ID 0
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("0");

        // Act: Request access to resource 0L
        boolean canAccess = policy.canAccessOwnResource(0L, authentication);

        // Assert: Verify zero ID is handled correctly
        assertTrue(canAccess, "Zero ID should be correctly compared (not treated as falsy)");
    }

    /**
     * Validates that very large Long values (Long.MAX_VALUE) are correctly handled.
     * 
     * <p><b>Scenario</b>: Tests boundary condition with the maximum Long value (9223372036854775807L).
     * SQLdatabases often use Long as the primary key type.
     * 
     * <p><b>Expected Behavior</b>: The policy correctly compares large ID values without
     * overflow or precision loss.
     * 
     * <p><b>Robustness</b>: Ensures the policy works across the full range of Long values.
     */
    @Test
    @DisplayName("Large ID numbers are correctly compared")
    void testCanAccessOwnResource_LargeId() {
        // Arrange: Set up authenticated user with Long.MAX_VALUE as principal
        Long largeId = 9223372036854775807L; // Long.MAX_VALUE
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(largeId.toString());

        // Act: Request access to resource with large ID
        boolean canAccess = policy.canAccessOwnResource(largeId, authentication);

        // Assert: Verify large IDs are correctly compared
        assertTrue(canAccess, "Large ID numbers should be correctly compared");
    }

    /**
     * Validates graceful handling of principal strings with extra whitespace.
     * 
     * <p><b>Scenario</b>: Principal ID is " 123 " with leading/trailing whitespace.
     * This can occur due to data formatting issues or external system integration.
     * 
     * <p><b>Expected Behavior</b>: The policy denies access because String" 123 " does not
     * convert cleanly to Long 123 (NumberFormatException or mismatch).
     * 
     * <p><b>Security Note</b>: This test documents that whitespace causes denial, which is
     * a safe/secure default (fail-closed principle).
     */
    @Test
    @DisplayName("Extra whitespace in principal is handled safely")
    void testCanAccessOwnResource_PrincipalWithWhitespace() {
        // Arrange: Set up authenticated user with whitespace-padded principal ID
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(" 123 ");

        // Act: Request access (String with whitespace may fail conversion or mismatch)
        boolean canAccess = policy.canAccessOwnResource(123L, authentication);

        // Assert: Verify whitespace causes denial (fail-closed is secure)
        assertFalse(canAccess, "Whitespace in principal should result in denied access (fail-closed)");
    }

    /**
     * Validates behavior when authentication exists but principal is null.
     * 
     * <p><b>Scenario</b>: Authentication object exists and isAuthenticated() returns true,
     * but getPrincipal() returns null. This is an inconsistent/corrupt state.
     * 
     * <p><b>Expected Behavior</b>: The policy throws an exception (NullPointerException or custom exception)
     * rather than silently granting/denying access. This forces callers to handle the error explicitly.
     * 
     * <p><b>Security Principle</b>: Fail-closed with explicit error is preferable to silent failure.
     */
    @Test
    @DisplayName("Null principal in authenticated object throws exception")
    void testCanAccessOwnResource_AuthenticatedButNullPrincipal() {
        // Arrange: Authenticated but null principal (inconsistent state)
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(null);

        // Act & Assert: Request should throw exception to prevent silent errors
        assertThrows(Exception.class, () -> {
            policy.canAccessOwnResource(123L, authentication);
        }, "Null principal should throw exception (fail-closed with explicit error)");
    }
}

