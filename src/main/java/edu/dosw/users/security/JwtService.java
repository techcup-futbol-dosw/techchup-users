package edu.dosw.users.security;

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

@Service
public class JwtService {

    private static final String ROLES_CLAIM = "roles";
    private static final String PERMISSIONS_CLAIM = "permissions";
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";

    private final SecretKey secretKey;

    public JwtService(@Value("${security.jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    /**
     * Validates that the token:
     * - has a correct signature
     * - is not expired
     * - is of type ACCESS
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
     * Extracts the account/user id from the JWT subject (sub).
     *
     * IMPORTANT:
     * If your team prefers the name extractAccountId(...) instead of extractUserId(...),
     * you can rename this method. Just keep it consistent across the service.
     */
    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Alternative alias if your team is already using "account" terminology.
     */
    public String extractAccountId(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extracts the "roles" claim as a List<String>.
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
     * Extracts the "permissions" claim as a List<String>.
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
     * Extracts the token type (expected: ACCESS).
     */
    public String extractTokenType(String token) {
        return extractAllClaims(token).get(TOKEN_TYPE_CLAIM, String.class);
    }

    /**
     * Parses and returns all claims from the JWT.
     * This method throws if the token is malformed, expired, or signature is invalid.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

