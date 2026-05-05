package edu.dosw.users.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service responsible for validating JWTs and extracting commonly used claims.
 *
 * <p>Expectations:
 * <ul>
 *   <li>Configuration property `security.jwt.secret` contains a Base64-encoded HMAC secret.</li>
 *   <li>Tokens validated by this service must include a {@code tokenType} claim
 *       set to {@code "ACCESS"} to be considered valid for API access.</li>
 * </ul>
 *
 * The class is thread-safe: it stores an immutable {@link javax.crypto.SecretKey}
 * constructed at startup and uses stateless parsing operations for each token.
 */
@Service
public class JwtService {

    private static final String ROLES_CLAIM = "roles";
    private static final String PERMISSIONS_CLAIM = "permissions";
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";

    private final SecretKey secretKey;

    /**
     * Construct the service using the Base64-encoded secret from configuration.
     * The secret is decoded and turned into an HMAC signing key used to verify
     * incoming JWT signatures.
     *
     * @param secret Base64-encoded HMAC secret (property `security.jwt.secret`)
     */
    public JwtService(@Value("${security.jwt.secret}") String secret) {
        // Keys.hmacShaKeyFor expects a raw byte array representing the secret
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    /**
     * Returns true when the provided JWT meets these conditions:
     * <ol>
     *   <li>signature is valid for the configured secret</li>
     *   <li>token is not expired</li>
     *   <li>{@code tokenType} claim equals {@code "ACCESS"}</li>
     * </ol>
     *
     * @param token compact JWT string (must not be null)
     * @return {@code true} when token is valid and of type {@code ACCESS}; {@code false} otherwise
     */
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return ACCESS_TOKEN_TYPE.equals(claims.get(TOKEN_TYPE_CLAIM, String.class));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Returns the JWT subject (the configured user/account id stored in {@code sub}).
     *
     * @param token compact JWT string
     * @return subject claim (may be {@code null} if not present)
     * @throws io.jsonwebtoken.JwtException when the token is invalid or cannot be parsed
     */
    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Alias for {@link #extractUserId(String)} kept for teams using "account" terminology.
     */
    public String extractAccountId(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extracts the {@code roles} claim and returns it as a list of strings.
     * If the claim is missing or not an array, an empty list is returned.
     *
     * @param token compact JWT string
     * @return list of role names or empty list
     * @throws io.jsonwebtoken.JwtException when the token cannot be parsed
     */
    public List<String> extractRoles(String token) {
        Object rolesObj = extractAllClaims(token).get(ROLES_CLAIM);

        if (rolesObj instanceof List<?> rawList) {
            List<String> roles = new ArrayList<>();
            for (Object item : rawList) {
                roles.add(String.valueOf(item));
            }
            return roles;
        }

        return Collections.emptyList();
    }

    /**
     * Extracts the {@code permissions} claim and returns it as a list of strings.
     * If the claim is missing or not an array, an empty list is returned.
     *
     * @param token compact JWT string
     * @return list of permission names or empty list
     * @throws io.jsonwebtoken.JwtException when the token cannot be parsed
     */
    public List<String> extractPermissions(String token) {
        Object permissionsObj = extractAllClaims(token).get(PERMISSIONS_CLAIM);

        if (permissionsObj instanceof List<?> rawList) {
            List<String> permissions = new ArrayList<>();
            for (Object item : rawList) {
                permissions.add(String.valueOf(item));
            }
            return permissions;
        }

        return Collections.emptyList();
    }

    /**
     * Reads the configured {@code tokenType} claim (typically {@code ACCESS} or other types).
     *
     * @param token compact JWT string
     * @return tokenType claim as string or {@code null} when absent
     * @throws io.jsonwebtoken.JwtException when the token cannot be parsed
     */
    public String extractTokenType(String token) {
        return extractAllClaims(token).get(TOKEN_TYPE_CLAIM, String.class);
    }

    /**
     * Parses the JWT and returns its claims. Throws a JwtException on invalid tokens.
     *
     * Note: callers should handle {@link io.jsonwebtoken.JwtException} to
     * control the authentication flow (this service leaves that responsibility
     * to the caller or higher-level filters).
     *
     * @param token compact JWT string
     * @return parsed claims
     * @throws io.jsonwebtoken.JwtException when the token is invalid, expired or signature verification fails
     */
    private Claims extractAllClaims(String token) {
        // Parse and validate the signed JWT using the preconfigured secret key.
        // This will throw a JwtException for malformed, expired or invalid tokens.
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

