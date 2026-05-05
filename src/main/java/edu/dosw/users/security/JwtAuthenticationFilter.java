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

@Component
/**
 * Filter that authenticates requests based on a Bearer JWT token.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Extract the Bearer token from the Authorization header</li>
 *   <li>Validate the token using {@code JwtService}</li>
 *   <li>Extract user id, roles and permissions and populate the Spring Security context</li>
 * </ul>
 */
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
            roles.forEach(role ->
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role))
            );
        }

        if (permissions != null) {
            permissions.forEach(permission ->
                    authorities.add(new SimpleGrantedAuthority(permission))
            );
        }

        return authorities;
    }
}

