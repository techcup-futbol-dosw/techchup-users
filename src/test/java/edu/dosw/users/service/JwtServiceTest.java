package edu.dosw.users.service;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for JwtService.
 *
 * <p>This test class validates the core functionality of JWT token handling including:
 * <ul>
 *   <li><b>Token Validation</b>: Valid tokens, expired tokens, invalid signatures, missing claims</li>
 *   <li><b>Claims Extraction</b>: userId, accountId, roles, permissions, and tokenType</li>
 *   <li><b>Edge Cases</b>: Missing claims, non-array claims, empty arrays, null values</li>
 *   <li><b>Error Handling</b>: Malformed tokens, JWT exceptions</li>
 * </ul>
 *
 * <p><b>Test Strategy</b>: All tests use a fixed 256-bit HMAC secret encoded in Base64 to ensure
 * reproducible results. Tokens are generated with controlled expiration times and claims
 * for deterministic test behavior.
 *
 * <p>Each test follows the AAA pattern (Arrange-Act-Assert) for clarity and maintainability.
 *
 * @see JwtService
 */
class JwtServiceTest {

    private JwtService jwtService;
    private String secret;
    private SecretKey secretKey;

    /**
     * Setup method executed before each test case.
     * Initializes a fresh JwtService instance with a test-specific Base64-encoded secret.
     * This ensures test isolation and prevents state carryover between tests.
     */
    @BeforeEach
    void setUp() {
        // Generate a 256-bit (32-byte) HMAC secret for deterministic token signing
        secretKey = Keys.hmacShaKeyFor(new byte[32]);
        // Encode the raw bytes to Base64 as expected by JwtService constructor
        secret = Encoders.BASE64.encode(secretKey.getEncoded());

        // Instantiate JwtService with the test secret
        jwtService = new JwtService(secret);
    }

    // ========== Token Validation Tests ==========

    /**
     * Validates that a well-formed token with ACCESS type is correctly recognized as valid.
     * 
     * <p><b>Scenario</b>: Token signed with the correct secret, not expired, containing
     * the required {@code tokenType} claim set to {@code "ACCESS"}.
     * 
     * <p><b>Expected Result</b>: {@code isTokenValid()} returns {@code true}.
     */
    @Test
    @DisplayName("Valid token with ACCESS type should be recognized as valid")
    void testIsTokenValid_ValidAccessToken() {
        // Arrange: Generate a valid token valid for 1 hour
        String validToken = generateToken(true, Instant.now().plusSeconds(3600), "ACCESS");

        // Act: Call validation method
        boolean result = jwtService.isTokenValid(validToken);

        // Assert: Token should be valid
        assertTrue(result, "Valid ACCESS token should be recognized");
    }

    /**
     * Validates that tokens with non-ACCESS tokenType are rejected.
     * 
     * <p><b>Scenario</b>: Token is well-formed but has {@code tokenType} set to
     * something other than {@code "ACCESS"} (e.g., {@code "REFRESH"}).
     * 
     * <p><b>Expected Result</b>: {@code isTokenValid()} returns {@code false}.
     */
    @Test
    @DisplayName("Token with non-ACCESS type should be invalid")
    void testIsTokenValid_NonAccessTokenType() {
        // Arrange: Generate a token with REFRESH type
        String refreshToken = generateToken(true, Instant.now().plusSeconds(3600), "REFRESH");

        // Act: Call validation method
        boolean result = jwtService.isTokenValid(refreshToken);

        // Assert: Token should be invalid
        assertFalse(result, "Non-ACCESS token type should be invalid");
    }

    /**
     * Validates that expired tokens are correctly detected and rejected.
     * 
     * <p><b>Scenario</b>: Token exists but has a past expiration timestamp.
     * 
     * <p><b>Expected Result</b>: {@code isTokenValid()} returns {@code false}
     * (token is considered expired by JJWT library).
     */
    @Test
    @DisplayName("Expired token should be invalid")
    void testIsTokenValid_ExpiredToken() {
        // Arrange: Generate a token that expired 1 second ago
        String expiredToken = generateToken(true, Instant.now().minusSeconds(1), "ACCESS");

        // Act: Call validation method
        boolean result = jwtService.isTokenValid(expiredToken);

        // Assert: Token should be invalid due to expiration
        assertFalse(result, "Expired token should be invalid");
    }

    /**
     * Validates that malformed token strings are safely handled.
     * 
     * <p><b>Scenario</b>: Input is not a valid JWT (e.g., random text).
     * 
     * <p><b>Expected Result</b>: {@code isTokenValid()} catches the exception
     * and returns {@code false} (does not throw).
     */
    @Test
    @DisplayName("Malformed token should be invalid")
    void testIsTokenValid_MalformedToken() {
        // Arrange: Create a string that is not a valid JWT
        String malformedToken = "not.a.valid.jwt.token";

        // Act: Call validation method (should not throw)
        boolean result = jwtService.isTokenValid(malformedToken);

        // Assert: Token should be invalid (exception caught internally)
        assertFalse(result, "Malformed token should be invalid");
    }

    /**
     * Validates that tokens signed with a different secret are rejected.
     * 
     * <p><b>Scenario</b>: Token is signed with a different HMAC secret than the one
     * configured in this JwtService instance.
     * 
     * <p><b>Expected Result</b>: {@code isTokenValid()} returns {@code false}
     * (signature verification fails).
     */
    @Test
    @DisplayName("Token with wrong signature should be invalid")
    void testIsTokenValid_WrongSignature() {
        // Arrange: Create a different secret (alter first byte to ensure difference)
        byte[] differentSecret = new byte[32];
        differentSecret[0] = 1;
        SecretKey otherKey = Keys.hmacShaKeyFor(differentSecret);
        // Generate token signed with the different key
        String tokenWithWrongSecret = Jwts.builder()
                .subject("user123")
                .expiration(new Date(Instant.now().plusSeconds(3600).toEpochMilli()))
                .claim("tokenType", "ACCESS")
                .signWith(otherKey)
                .compact();

        // Act: Call validation method (service will use its own secret to verify)
        boolean result = jwtService.isTokenValid(tokenWithWrongSecret);

        // Assert: Signature verification should fail
        assertFalse(result, "Token signed with different key should be invalid");
    }

    /**
     * Validates that tokens lacking the required tokenType claim are rejected.
     * 
     * <p><b>Scenario</b>: Token is well-formed and properly signed but missing
     * the {@code tokenType} claim entirely.
     * 
     * <p><b>Expected Result</b>: {@code isTokenValid()} returns {@code false}.
     */
    @Test
    @DisplayName("Token without tokenType claim should be invalid")
    void testIsTokenValid_MissingTokenTypeClaim() {
        // Arrange: Create a valid token without the tokenType claim
        String tokenNoType = Jwts.builder()
                .subject("user123")
                .expiration(new Date(Instant.now().plusSeconds(3600).toEpochMilli()))
                .signWith(secretKey)
                .compact();

        // Act: Call validation method
        boolean result = jwtService.isTokenValid(tokenNoType);

        // Assert: Token should be invalid (required claim is missing)
        assertFalse(result, "Token without tokenType should be invalid");
    }

    // ========== User ID Extraction Tests ==========

    /**
     * Validates that userId (JWT subject) is correctly extracted from a well-formed token.
     * 
     * <p><b>Scenario</b>: Token contains a subject claim with a specific user identifier.
     * 
     * <p><b>Expected Result</b>: {@code extractUserId()} returns the exact subject value.
     */
    @Test
    @DisplayName("Extract userId from valid token")
    void testExtractUserId_ValidToken() {
        // Arrange: Define expected user ID
        String expectedUserId = "user-uuid-12345";
        // Generate token with this user ID as the subject
        String token = Jwts.builder()
                .subject(expectedUserId)
                .expiration(new Date(Instant.now().plusSeconds(3600).toEpochMilli()))
                .signWith(secretKey)
                .compact();

        // Act: Extract the userId
        String userId = jwtService.extractUserId(token);

        // Assert: Returned value matches the subject
        assertEquals(expectedUserId, userId, "Extracted userId should match token subject");
    }

    /**
     * Validates that the {@code extractAccountId()} alias method works correctly.
     * This method is provided for teams using "account" terminology.
     * 
     * <p><b>Scenario</b>: Token contains a subject claim, calling the alternate method name.
     * 
     * <p><b>Expected Result</b>: {@code extractAccountId()} returns the same subject value.
     */
    @Test
    @DisplayName("Extract account ID (alias) from valid token")
    void testExtractAccountId_ValidToken() {
        // Arrange: Define expected account ID
        String expectedAccountId = "account-uuid-67890";
        // Generate token with this account ID as the subject
        String token = Jwts.builder()
                .subject(expectedAccountId)
                .expiration(new Date(Instant.now().plusSeconds(3600).toEpochMilli()))
                .signWith(secretKey)
                .compact();

        // Act: Extract the accountId using the alias method
        String accountId = jwtService.extractAccountId(token);

        // Assert: Returned value matches the subject
        assertEquals(expectedAccountId, accountId, "Extracted accountId should match token subject");
    }

    /**
     * Validates that invalid tokens cause JwtException when extracting userId.
     * 
     * <p><b>Scenario</b>: Input is not a valid JWT string.
     * 
     * <p><b>Expected Result</b>: {@code extractUserId()} throws {@code JwtException}
     * (unlike {@code isTokenValid()}, this method does not suppress exceptions).
     */
    @Test
    @DisplayName("Extract userId throws exception for invalid token")
    void testExtractUserId_InvalidToken() {
        // Arrange: Create invalid JWT
        String invalidToken = "invalid.token";

        // Act & Assert: Expect JwtException when attempting extraction
        assertThrows(JwtException.class, () -> jwtService.extractUserId(invalidToken),
                "Should throw JwtException for invalid token");
    }

    // ========== Roles Extraction Tests ==========

    /**
     * Validates that roles are correctly extracted when the token contains a roles array claim.
     * 
     * <p><b>Scenario</b>: Token contains a {@code roles} claim with a List of role names.
     * 
     * <p><b>Expected Result</b>: {@code extractRoles()} returns a list of strings matching the claim.
     */
    @Test
    @DisplayName("Extract roles from token with roles claim")
    void testExtractRoles_WithRolesClaim() {
        // Arrange: Define expected roles
        List<String> expectedRoles = List.of("ADMIN", "USER", "MODERATOR");
        // Generate token with roles claim
        String token = Jwts.builder()
                .subject("user123")
                .expiration(new Date(Instant.now().plusSeconds(3600).toEpochMilli()))
                .claim("roles", expectedRoles)
                .signWith(secretKey)
                .compact();

        // Act: Extract roles
        List<String> roles = jwtService.extractRoles(token);

        // Assert: Returned list matches the claim
        assertEquals(expectedRoles, roles, "Extracted roles should match claim");
    }

    /**
     * Validates that an empty list is returned when the roles claim is absent.
     * 
     * <p><b>Scenario</b>: Token exists but does not contain a {@code roles} claim.
     * 
     * <p><b>Expected Result</b>: {@code extractRoles()} returns an empty, unmodifiable list.
     */
    @Test
    @DisplayName("Return empty list when roles claim is missing")
    void testExtractRoles_MissingClaim() {
        // Arrange: Create token without roles claim
        String token = Jwts.builder()
                .subject("user123")
                .expiration(new Date(Instant.now().plusSeconds(3600).toEpochMilli()))
                .signWith(secretKey)
                .compact();

        // Act: Extract roles (should not throw)
        List<String> roles = jwtService.extractRoles(token);

        // Assert: Should return empty list
        assertTrue(roles.isEmpty(), "Should return empty list when roles claim is missing");
    }

    /**
     * Validates graceful handling when roles claim is not an array/list.
     * 
     * <p><b>Scenario</b>: Token contains a {@code roles} claim that is a scalar value
     * (e.g., a single string) instead of a list.
     * 
     * <p><b>Expected Result</b>: {@code extractRoles()} returns an empty list (type mismatch is ignored).
     */
    @Test
    @DisplayName("Return empty list when roles claim is not an array")
    void testExtractRoles_NotAnArray() {
        // Arrange: Create token with roles as a string instead of list
        String token = Jwts.builder()
                .subject("user123")
                .expiration(new Date(Instant.now().plusSeconds(3600).toEpochMilli()))
                .claim("roles", "ADMIN")
                .signWith(secretKey)
                .compact();

        // Act: Extract roles (should handle type mismatch gracefully)
        List<String> roles = jwtService.extractRoles(token);

        // Assert: Should return empty list (not matching the expected list type)
        assertTrue(roles.isEmpty(), "Should return empty list when roles is not an array");
    }

    /**
     * Validates that an empty roles array is correctly handled.
     * 
     * <p><b>Scenario</b>: Token contains a {@code roles} claim with an empty list.
     * 
     * <p><b>Expected Result</b>: {@code extractRoles()} returns an empty list.
     */
    @Test
    @DisplayName("Extract empty roles array")
    void testExtractRoles_EmptyArray() {
        // Arrange: Create token with empty roles list
        String token = Jwts.builder()
                .subject("user123")
                .expiration(new Date(Instant.now().plusSeconds(3600).toEpochMilli()))
                .claim("roles", List.of())
                .signWith(secretKey)
                .compact();

        // Act: Extract roles
        List<String> roles = jwtService.extractRoles(token);

        // Assert: Should return empty list
        assertTrue(roles.isEmpty(), "Should return empty list for empty roles array");
    }

    // ========== Permissions Extraction Tests ==========

    /**
     * Validates that permissions are correctly extracted when the token contains a permissions array claim.
     * 
     * <p><b>Scenario</b>: Token contains a {@code permissions} claim with a List of permission strings.
     * 
     * <p><b>Expected Result</b>: {@code extractPermissions()} returns a list of strings matching the claim.
     */
    @Test
    @DisplayName("Extract permissions from token with permissions claim")
    void testExtractPermissions_WithPermissionsClaim() {
        // Arrange: Define expected permissions
        List<String> expectedPermissions = List.of("read:users", "write:users", "delete:users");
        // Generate token with permissions claim
        String token = Jwts.builder()
                .subject("user123")
                .expiration(new Date(Instant.now().plusSeconds(3600).toEpochMilli()))
                .claim("permissions", expectedPermissions)
                .signWith(secretKey)
                .compact();

        // Act: Extract permissions
        List<String> permissions = jwtService.extractPermissions(token);

        // Assert: Returned list matches the claim
        assertEquals(expectedPermissions, permissions, "Extracted permissions should match claim");
    }

    /**
     * Validates that an empty list is returned when the permissions claim is absent.
     * 
     * <p><b>Scenario</b>: Token exists but does not contain a {@code permissions} claim.
     * 
     * <p><b>Expected Result</b>: {@code extractPermissions()} returns an empty, unmodifiable list.
     */
    @Test
    @DisplayName("Return empty list when permissions claim is missing")
    void testExtractPermissions_MissingClaim() {
        // Arrange: Create token without permissions claim
        String token = Jwts.builder()
                .subject("user123")
                .expiration(new Date(Instant.now().plusSeconds(3600).toEpochMilli()))
                .signWith(secretKey)
                .compact();

        // Act: Extract permissions (should not throw)
        List<String> permissions = jwtService.extractPermissions(token);

        // Assert: Should return empty list
        assertTrue(permissions.isEmpty(), "Should return empty list when permissions claim is missing");
    }

    /**
     * Validates graceful handling when permissions claim is not an array/list.
     * 
     * <p><b>Scenario</b>: Token contains a {@code permissions} claim that is a scalar value
     * (e.g., a single string) instead of a list.
     * 
     * <p><b>Expected Result</b>: {@code extractPermissions()} returns an empty list (type mismatch is ignored).
     */
    @Test
    @DisplayName("Return empty list when permissions claim is not an array")
    void testExtractPermissions_NotAnArray() {
        // Arrange: Create token with permissions as a string instead of list
        String token = Jwts.builder()
                .subject("user123")
                .expiration(new Date(Instant.now().plusSeconds(3600).toEpochMilli()))
                .claim("permissions", "SUPER_ADMIN")
                .signWith(secretKey)
                .compact();

        // Act: Extract permissions (should handle type mismatch gracefully)
        List<String> permissions = jwtService.extractPermissions(token);

        // Assert: Should return empty list (not matching the expected list type)
        assertTrue(permissions.isEmpty(), "Should return empty list when permissions is not an array");
    }

    // ========== Token Type Extraction Tests ==========

    /**
     * Validates that the tokenType claim is correctly extracted from a valid token.
     * 
     * <p><b>Scenario</b>: Token contains a {@code tokenType} claim with a specific value.
     * 
     * <p><b>Expected Result</b>: {@code extractTokenType()} returns the claim value as a string.
     */
    @Test
    @DisplayName("Extract token type from valid token")
    void testExtractTokenType_ValidToken() {
        // Arrange: Define expected token type
        String expectedType = "ACCESS";
        // Generate token with this token type
        String token = generateToken(true, Instant.now().plusSeconds(3600), expectedType);

        // Act: Extract token type
        String tokenType = jwtService.extractTokenType(token);

        // Assert: Returned type matches the claim
        assertEquals(expectedType, tokenType, "Extracted tokenType should match claim");
    }

    /**
     * Validates that null is returned when the tokenType claim is absent.
     * 
     * <p><b>Scenario</b>: Token exists but does not contain a {@code tokenType} claim.
     * 
     * <p><b><b>Expected Result</b>: {@code extractTokenType()} returns {@code null}.
     */
    @Test
    @DisplayName("Return null when tokenType claim is missing")
    void testExtractTokenType_MissingClaim() {
        // Arrange: Create token without tokenType claim
        String token = Jwts.builder()
                .subject("user123")
                .expiration(new Date(Instant.now().plusSeconds(3600).toEpochMilli()))
                .signWith(secretKey)
                .compact();

        // Act: Extract token type (should not throw)
        String tokenType = jwtService.extractTokenType(token);

        // Assert: Should return null when claim is absent
        assertNull(tokenType, "Should return null when tokenType claim is missing");
    }

    // ========== Helper Methods ==========

    /**
     * Generates a test JWT token for use in test cases.
     * 
     * <p>This helper uses the pre-configured {@code secretKey} to ensure all generated
     * tokens are compatible with the test instance of JwtService.
     *
     * @param includeSubject whether to include a subject (user ID) claim in the token
     * @param expirationTime the instant at which the token should expire
     * @param tokenType the value for the {@code tokenType} claim (e.g., {@code "ACCESS"})
     * 
     * @return a compact JWT string (base64-encoded header.payload.signature)
     * 
     * <p><b>Note</b>: All generated tokens use the test secret key and are deterministic
     * based on the input parameters.
     */
    private String generateToken(boolean includeSubject, Instant expirationTime, String tokenType) {
        // Create JWT builder with expiration and tokenType
        var builder = Jwts.builder()
                .expiration(new Date(expirationTime.toEpochMilli()))
                .claim("tokenType", tokenType);

        // Conditionally add subject (user ID)
        if (includeSubject) {
            builder.subject("test-user-123");
        }

        // Sign and compact (produce the final JWT string)
        return builder.signWith(secretKey).compact();
    }
}

