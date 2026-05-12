package edu.dosw.users.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration active only when the {@code local} profile is enabled.
 *
 * <p>Permits all requests without authentication so that developers can call the
 * API freely from Postman or Swagger without needing a JWT token during local
 * development. This bean is intentionally excluded from JaCoCo coverage because
 * it cannot be integration-tested without a running MongoDB instance.</p>
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

    @Bean
    @Order(1)
    public SecurityFilterChain localFilterChain(HttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }
}