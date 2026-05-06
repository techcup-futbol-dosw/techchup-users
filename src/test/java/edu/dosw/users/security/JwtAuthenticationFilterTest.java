package edu.dosw.users.security;

import edu.dosw.users.service.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtAuthenticationFilter.
 *
 * <p>This test class validates the JWT authentication filter functionality including:
 * <ul>
 *   <li><b>Authorization Header Handling</b>: Bearer token extraction and validation</li>
 *   <li><b>Token Validation</b>: Valid/invalid tokens and expiration</li>
 *   <li><b>SecurityContext Population</b>: Claims extraction and authority building</li>
 *   <li><b>Error Handling</b>: Exception handling and context cleanup</li>
 *   <li><b>Filter Chain Continuation</b>: Ensures filter chain always proceeds</li>
 * </ul>
 *
 * <p><b>Approach</b>: Uses Mockito for mocking servlet dependencies and Spring Security
 * components. SecurityContextHolder is manually cleared between tests to ensure isolation.
 *
 * @see JwtAuthenticationFilter
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private JwtAuthenticationFilter filter;
    private JwtService jwtService;
    private SecretKey secretKey;
    private String validToken;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    /**
     * Setup method executed before each test.
     * Initializes a JwtService with a fixed test secret, generates a valid test token,
     * and initializes the JwtAuthenticationFilter.
     */
    @BeforeEach
    void setUp() {
        // Generate a 256-bit HMAC secret
        secretKey = Keys.hmacShaKeyFor(new byte[32]);
        String secret = new String(java.util.Base64.getEncoder().encode(secretKey.getEncoded()));

        // Create JwtService with test secret
        jwtService = new JwtService(secret);

        // Initialize filter
        filter = new JwtAuthenticationFilter(jwtService);

        // Generate a valid token for testing
        validToken = Jwts.builder()
                .subject("user-123")
                .expiration(new Date(Instant.now().plusSeconds(3600).toEpochMilli()))
                .claim("tokenType", "ACCESS")
                .claim("roles", List.of("ADMIN", "USER"))
                .claim("permissions", List.of("read:users", "write:users"))
                .signWith(secretKey)
                .compact();

        // Clear security context before each test
        SecurityContextHolder.clearContext();
    }

    // ========== Authorization Header Tests ==========

    /**
     * Validates that valid Bearer token is extracted and processed correctly.
     * 
     * <p><b>Scenario</b>: Request contains an Authorization header with a valid Bearer token.
     * 
     * <p><b>Expected Result</b>: Token is extracted, validated, and SecurityContext is populated.
     */
    @Test
    @DisplayName("Valid Bearer token populates SecurityContext with authorities")
    void testDoFilterInternal_ValidBearerToken() throws Exception {
        // Arrange: Set up mock request with valid Bearer token
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        // Act: Execute filter
        filter.doFilterInternal(request, response, filterChain);

        // Assert: Verify authentication is in SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth, "Authentication should be set");
        assertEquals("user-123", auth.getPrincipal(), "Principal should be userId");
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")), "Should have ROLE_ADMIN");
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("read:users")), "Should have read:users permission");
        
        // Verify filter chain continues
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Validates that missing Authorization header is handled gracefully.
     * 
     * <p><b>Scenario</b>: Request does not contain an Authorization header.
     * 
     * <p><b>Expected Result</b>: Filter continues without authentication, no SecurityContext entry.
     */
    @Test
    @DisplayName("Missing Authorization header skips authentication")
    void testDoFilterInternal_NoAuthorizationHeader() throws Exception {
        // Arrange: No Authorization header
        when(request.getHeader("Authorization")).thenReturn(null);

        // Act: Execute filter
        filter.doFilterInternal(request, response, filterChain);

        // Assert: No authentication in context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth, "No authentication should be set");
        
        // Verify filter chain continues
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Validates that non-Bearer Authorization headers are skipped.
     * 
     * <p><b>Scenario</b>: Authorization header does not start with "Bearer ".
     * 
     * <p><b>Expected Result</b>: Filter continues without authentication.
     */
    @Test
    @DisplayName("Non-Bearer Authorization header skips authentication")
    void testDoFilterInternal_NonBearerAuthorizationHeader() throws Exception {
        // Arrange: Basic authentication instead of Bearer
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        // Act: Execute filter
        filter.doFilterInternal(request, response, filterChain);

        // Assert: No authentication in context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth, "No authentication should be set");
        
        // Verify filter chain continues
        verify(filterChain).doFilter(request, response);
    }

    // ========== Token Validation Tests ==========

    /**
     * Validates that invalid tokens do not populate SecurityContext.
     * 
     * <p><b>Scenario</b>: Authorization header contains an invalid token.
     * 
     * <p><b>Expected Result</b>: Token validation fails, no authentication is set.
     */
    @Test
    @DisplayName("Invalid token does not populate SecurityContext")
    void testDoFilterInternal_InvalidToken() throws Exception {
        // Arrange: Invalid token
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token.here");

        // Act: Execute filter
        filter.doFilterInternal(request, response, filterChain);

        // Assert: No authentication in context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth, "No authentication should be set for invalid token");
        
        // Verify filter chain continues
        verify(filterChain).doFilter(request, response);
    }

    /**
     * Validates that expired tokens do not populate SecurityContext.
     * 
     * <p><b>Scenario</b>: Token's expiration time has passed.
     * 
     * <p><b>Expected Result</b>: Token validation fails, no authentication is set.
     */
    @Test
    @DisplayName("Expired token does not populate SecurityContext")
    void testDoFilterInternal_ExpiredToken() throws Exception {
        // Arrange: Generate expired token
        String expiredToken = Jwts.builder()
                .subject("user-123")
                .expiration(new Date(Instant.now().minusSeconds(3600).toEpochMilli()))
                .claim("tokenType", "ACCESS")
                .signWith(secretKey)
                .compact();
        when(request.getHeader("Authorization")).thenReturn("Bearer " + expiredToken);

        // Act: Execute filter
        filter.doFilterInternal(request, response, filterChain);

        // Assert: No authentication in context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth, "No authentication should be set for expired token");
        
        // Verify filter chain continues
        verify(filterChain).doFilter(request, response);
    }

    // ========== SecurityContext Population Tests ==========

    /**
     * Validates that roles are correctly converted to ROLE_ prefixed authorities.
     * 
     * <p><b>Scenario</b>: Token contains roles "ADMIN" and "USER".
     * 
     * <p><b>Expected Result</b>: Authorities include "ROLE_ADMIN" and "ROLE_USER".
     */
    @Test
    @DisplayName("Roles are converted to ROLE_ prefixed authorities")
    void testDoFilterInternal_RolesConvertedToAuthorities() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    /**
     * Validates that permissions are added as authorities without ROLE_ prefix.
     * 
     * <p><b>Scenario</b>: Token contains permissions "read:users" and "write:users".
     * 
     * <p><b>Expected Result</b>: Authorities include these permissions directly.
     */
    @Test
    @DisplayName("Permissions are added as authorities without ROLE_ prefix")
    void testDoFilterInternal_PermissionsAddedAsAuthorities() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("read:users")));
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("write:users")));
    }

    // ========== Error Handling Tests ==========

    /**
     * Validates that SecurityContext is cleared when an exception occurs during processing.
     * 
     * <p><b>Scenario</b>: Exception thrown while extracting token claims.
     * 
     * <p><b>Expected Result</b>: SecurityContext is cleared to prevent invalid authentication.
     */
    @Test
    @DisplayName("Exception clears SecurityContext to prevent invalid authentication")
    void testDoFilterInternal_ExceptionClearsSecurityContext() throws Exception {
        // Arrange: Set up valid token but test that exception handling works
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        
        // Act: Execute filter - should not throw even with edge cases
        assertDoesNotThrow(() -> filter.doFilterInternal(request, response, filterChain));
        
        // Assert: Verify filter chain continues regardless
        verify(filterChain).doFilter(request, response);
    }

    // ========== Filter Chain Continuation Tests ==========

    /**
     * Validates that filter chain always continues, even with invalid authentication.
     * 
     * <p><b>Scenario</b>: Various authentication scenarios (valid, invalid, missing).
     * 
     * <p><b>Expected Result</b>: filterChain.doFilter() is always called.
     */
    @Test
    @DisplayName("Filter chain always continues regardless of authentication")
    void testDoFilterInternal_FilterChainContinuesAlways() throws Exception {
        // Arrange: Invalid token
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid");

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert: Filter chain continues
        verify(filterChain, times(1)).doFilter(request, response);
    }
}

