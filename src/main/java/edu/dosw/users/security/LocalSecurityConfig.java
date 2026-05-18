package edu.dosw.users.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration active only when the {@code local} profile is enabled.
 *
 * <p>Installs the {@link JwtAuthenticationFilter} so that {@code @PreAuthorize}
 * annotations on controllers can resolve the authenticated user from the Bearer
 * token. All requests are still permitted at the filter-chain level, which means
 * Swagger and H2 console remain accessible without a token.</p>
 *
 * <p><strong>CSRF note (S4502):</strong> CSRF protection is disabled because this
 * is a stateless JWT REST API. Authentication is carried in the
 * {@code Authorization: Bearer} header — never in session cookies — so browsers
 * cannot be tricked into sending credentials automatically on cross-site
 * requests.</p>
 */
@Configuration
@Profile("local")
@SuppressWarnings("java:S4502")
public class LocalSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public LocalSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain localFilterChain(HttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}