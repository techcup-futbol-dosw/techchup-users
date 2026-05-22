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
 * Servicio responsable de validar JWT y extraer los claims de uso frecuente.
 *
 * <p>Requisitos:
 * <ul>
 *   <li>La propiedad de configuración {@code security.jwt.secret} debe contener un secreto
 *       HMAC codificado en Base64.</li>
 *   <li>Los tokens validados por este servicio deben incluir el claim {@code tokenType}
 *       con valor {@code "ACCESS"} para ser considerados válidos para el acceso a la API.</li>
 * </ul>
 *
 * <p>La clase es thread-safe: almacena un {@link javax.crypto.SecretKey} inmutable
 * construido al inicio y utiliza operaciones de parseo sin estado para cada token.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
@Service
public class JwtService {

    private static final String ROLES_CLAIM = "roles";
    private static final String PERMISSIONS_CLAIM = "permissions";
    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN_TYPE = "ACCESS";

    private final SecretKey secretKey;

    /**
     * Construye el servicio usando el secreto codificado en Base64 de la configuración.
     * El secreto se decodifica y transforma en una clave de firma HMAC usada para
     * verificar las firmas de los JWT entrantes.
     *
     * @param secretFromProperty secreto HMAC codificado en Base64 inyectado desde {@code security.jwt.secret};
     */
    public JwtService(@Value("${security.jwt.secret:}") String secretFromProperty) {
        // Azure App Service can fail to resolve ${...} placeholders for env vars,
        // so fall back to System.getenv() directly (same pattern as MongoConfig).
        String secret = secretFromProperty;
        if (secret == null || secret.isBlank()) {
            secret = System.getenv("SECURITY_JWT_SECRET");
        }
        // JWT_SECRET is the env var used by the Identity Service — try it as fallback
        // so both services share the same signing key in Azure without extra config.
        if (secret == null || secret.isBlank()) {
            secret = System.getenv("JWT_SECRET");
        }
        if (secret == null || secret.isBlank()) {
            secret = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        }
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    /**
     * Retorna {@code true} cuando el JWT proporcionado cumple las siguientes condiciones:
     * <ol>
     *   <li>La firma es válida para el secreto configurado.</li>
     *   <li>El token no ha expirado.</li>
     *   <li>El claim {@code tokenType} es igual a {@code "ACCESS"}.</li>
     * </ol>
     *
     * @param token cadena JWT compacta (no debe ser {@code null})
     * @return {@code true} si el token es válido y de tipo {@code ACCESS}; {@code false} en caso contrario
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
     * Retorna el sujeto del JWT (el id de usuario/cuenta almacenado en el claim {@code sub}).
     *
     * @param token cadena JWT compacta
     * @return claim de sujeto (puede ser {@code null} si no está presente)
     * @throws io.jsonwebtoken.JwtException si el token es inválido o no puede parsearse
     */
    public String extractUserId(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Alias de {@link #extractUserId(String)} mantenido para equipos que usan la terminología "account".
     *
     * @param token cadena JWT compacta
     * @return claim de sujeto del token
     * @throws io.jsonwebtoken.JwtException si el token es inválido o no puede parsearse
     */
    public String extractAccountId(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Extrae el claim {@code roles} y lo retorna como lista de cadenas.
     * Si el claim está ausente o no es un array, se retorna una lista vacía.
     *
     * @param token cadena JWT compacta
     * @return lista de nombres de roles, o lista vacía si no hay roles
     * @throws io.jsonwebtoken.JwtException si el token no puede parsearse
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
     * Extrae el claim {@code permissions} y lo retorna como lista de cadenas.
     * Si el claim está ausente o no es un array, se retorna una lista vacía.
     *
     * @param token cadena JWT compacta
     * @return lista de nombres de permisos, o lista vacía si no hay permisos
     * @throws io.jsonwebtoken.JwtException si el token no puede parsearse
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
     * Lee el claim {@code tokenType} configurado (típicamente {@code ACCESS} u otros tipos).
     *
     * @param token cadena JWT compacta
     * @return valor del claim {@code tokenType} como cadena, o {@code null} si está ausente
     * @throws io.jsonwebtoken.JwtException si el token no puede parsearse
     */
    public String extractTokenType(String token) {
        return extractAllClaims(token).get(TOKEN_TYPE_CLAIM, String.class);
    }

    /**
     * Parsea el JWT y retorna sus claims. Lanza {@link io.jsonwebtoken.JwtException} en tokens inválidos.
     *
     * <p>Los llamadores deben manejar {@link io.jsonwebtoken.JwtException} para controlar el flujo
     * de autenticación; este servicio deja esa responsabilidad al llamador o a los filtros de nivel superior.</p>
     *
     * @param token cadena JWT compacta
     * @return claims parseados del token
     * @throws io.jsonwebtoken.JwtException si el token es inválido, ha expirado o falla la verificación de firma
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

