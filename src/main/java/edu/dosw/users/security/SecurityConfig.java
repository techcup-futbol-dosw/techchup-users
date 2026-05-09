package edu.dosw.users.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * Central security configuration for the service.
     *
     * <p>Configures stateless JWT-based authentication by registering the
     * {@link JwtAuthenticationFilter} before Spring Security's
     * {@link UsernamePasswordAuthenticationFilter}.
     * It also defines exception handling and which endpoints are public.</p>
     */

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationEntryPointImpl authenticationEntryPoint;
    private final AccessDeniedHandlerImpl accessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          AuthenticationEntryPointImpl authenticationEntryPoint,
                          AccessDeniedHandlerImpl accessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    @Profile("local")
    @Order(1)
    public SecurityFilterChain localFilterChain(HttpSecurity http) {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        try {
            return http.build();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to build the local security filter chain", ex);
        }
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) {
        // CSRF disabled: stateless JWT API — no session cookies, so CSRF protection is unnecessary.
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> {})

                // Use stateless session management: every request must carry auth info
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configure handlers for auth failures and access denied
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // Public endpoints (API docs) and require authentication for the rest
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                                // Add any public endpoints that this service may have.
                        ).permitAll()
                        .anyRequest().authenticated()
                )

                // Register JWT filter before the standard username/password filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        try {
            return http.build();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to build the security filter chain", ex);
        }
    }
}

