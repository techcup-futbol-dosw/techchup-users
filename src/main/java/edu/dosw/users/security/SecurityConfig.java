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
 * {@link UsernamePasswordAuthenticationFilter}.</p>
 *
 * <p>Control de acceso por rol (roles exactos del JWT):
 * <ul>
 *   <li>{@code ADMIN}   — acceso completo a todos los endpoints administrativos.</li>
 *   <li>{@code CAPTAIN} — puede buscar jugadores, consultar por identificación y gestionar invitaciones.</li>
 *   <li>Cualquier usuario autenticado — puede leer/actualizar su propio perfil y sport-profile.</li>
 * </ul>
 * La autorización detallada por endpoint se aplica mediante {@code @PreAuthorize}
 * con {@code hasRole()} e {@code isAuthenticated()}.
 * </p>
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
     * Cadena de filtros para cualquier perfil excepto {@code local} (incluye tests y producción).
     *
     * <p>CSRF deshabilitado: API REST sin estado con JWT — sin cookies de sesión.</p>
     *
     * @param http constructor de configuración HTTP de Spring Security
     * @return cadena de filtros de seguridad configurada
     */
    @Bean
    @Profile("!local")
    @SuppressWarnings("java:S4502")
    public SecurityFilterChain prodFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> {})
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        // Documentación pública
                        .requestMatchers(
                                "/",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // El resto requiere autenticación; el control de roles queda en @PreAuthorize
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Cadena de filtros para desarrollo local ({@code local}).
     *
     * <p>Permite acceso sin autenticación a la consola H2 y Swagger.
     * Los demás endpoints siguen requiriendo token JWT para que las pruebas
     * con Postman funcionen igual que en producción.</p>
     *
     * @param http constructor de configuración HTTP de Spring Security
     * @return cadena de filtros de seguridad para entorno local
     */
    @Bean
    @Profile("local")
    @SuppressWarnings("java:S4502")
    public SecurityFilterChain localFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/h2-console/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}