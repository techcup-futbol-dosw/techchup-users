package edu.dosw.users.security;

import edu.dosw.users.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Filtro que autentica las solicitudes basándose en un token JWT Bearer.
 *
 * <p>Responsabilidades:
 * <ul>
 *   <li>Extraer el token Bearer del encabezado {@code Authorization}.</li>
 *   <li>Validar el token mediante {@link edu.dosw.users.service.JwtService}.</li>
 *   <li>Extraer el id de usuario, roles y permisos, y poblar el contexto de Spring Security.</li>
 * </ul>
 *
 * @author CodeForge
 * @since 1.0
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // If no Authorization header or not a Bearer token, skip authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract raw token (after "Bearer ")
        String token = authHeader.substring(7);

        try {
            // Validate token signature, expiration and token-type
            if (!jwtService.isTokenValid(token)) {
                filterChain.doFilter(request, response);
                return;
            }

            // Only populate SecurityContext when not already authenticated
            if (SecurityContextHolder.getContext().getAuthentication() == null) {

                // Read claims needed for authorization
                String userId = jwtService.extractUserId(token);
                List<String> roles = jwtService.extractRoles(token);
                List<String> permissions = jwtService.extractPermissions(token);

                // Map roles/permissions to GrantedAuthority and create Authentication
                Collection<GrantedAuthority> authorities = buildAuthorities(roles, permissions);
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (Exception ex) {
            // On any error we clear the context to ensure no invalid authentication remains
            SecurityContextHolder.clearContext();
        }

        // Continue filter chain regardless of authentication outcome
        filterChain.doFilter(request, response);
    }

    private Collection<GrantedAuthority> buildAuthorities(List<String> roles, List<String> permissions) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        if (roles != null) {
            roles.forEach(role -> {
                // Normalize: uppercase + strip any existing ROLE_ prefix to avoid double-prefixing
                // and handle lowercase roles from some identity providers (e.g. "captain" → "ROLE_CAPTAIN")
                String upper = role.toUpperCase();
                String bare = upper.startsWith("ROLE_") ? upper.substring(5) : upper;
                authorities.add(new SimpleGrantedAuthority("ROLE_" + bare));
            });
        }

        if (permissions != null) {
            permissions.forEach(permission ->
                    authorities.add(new SimpleGrantedAuthority(permission))
            );
        }

        return authorities;
    }
}

