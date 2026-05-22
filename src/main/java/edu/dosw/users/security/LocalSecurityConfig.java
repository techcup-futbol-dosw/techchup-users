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
 * Configuración de seguridad activa únicamente cuando el perfil {@code local} está habilitado.
 *
 * <p>Instala el {@link JwtAuthenticationFilter} para que las anotaciones {@code @PreAuthorize}
 * en los controladores puedan resolver el usuario autenticado desde el token Bearer. Todas las
 * solicitudes se permiten a nivel de cadena de filtros, lo que significa que Swagger y la consola
 * H2 permanecen accesibles sin token.</p>
 *
 * <p><strong>Nota CSRF (S4502):</strong> La protección CSRF está deshabilitada porque esta es
 * una API REST sin estado con JWT. La autenticación se transporta en el encabezado
 * {@code Authorization: Bearer} — nunca en cookies de sesión — por lo que los navegadores
 * no pueden ser engañados para enviar credenciales automáticamente en solicitudes entre sitios.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
@Configuration
@Profile("local")
@SuppressWarnings("java:S4502")
public class LocalSecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public LocalSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Configura la cadena de filtros de seguridad para el perfil {@code local}.
     * Permite todas las solicitudes sin autenticación obligatoria, pero instala
     * el filtro JWT para que los endpoints protegidos con {@code @PreAuthorize} funcionen.
     *
     * @param http constructor de configuración HTTP de Spring Security
     * @return cadena de filtros de seguridad local configurada
     */
    @Bean
    @Order(1)
    public SecurityFilterChain localPermitAllFilterChain(HttpSecurity http) {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}