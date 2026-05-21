package edu.dosw.users.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuración central de seguridad del servicio.
 *
 * <p>Establece autenticación sin estado basada en JWT registrando el
 * {@link JwtAuthenticationFilter} antes del filtro estándar de Spring Security
 * {@link UsernamePasswordAuthenticationFilter}. También define el manejo de
 * excepciones y qué endpoints son públicos.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

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

    /**
     * Configura la cadena de filtros de seguridad para todos los perfiles excepto {@code local}.
     *
     * <p>CSRF deshabilitado: API REST sin estado con JWT — sin cookies de sesión, por lo que
     * la protección CSRF es innecesaria.</p>
     *
     * @param http constructor de configuración HTTP de Spring Security
     * @return cadena de filtros de seguridad configurada
     */
    @Bean
    @Profile("!local")
    @SuppressWarnings("java:S4502")
    public SecurityFilterChain filterChain(HttpSecurity http) {
        // CSRF disabled: stateless JWT API — no session cookies, so CSRF protection is unnecessary.
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})

                // Use stateless session management: every request must carry auth info
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configure handlers for auth failures and access denied
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))

                // Public endpoints (API docs) and require authentication for the rest
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated())

                // Register JWT filter before the standard username/password filter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}

