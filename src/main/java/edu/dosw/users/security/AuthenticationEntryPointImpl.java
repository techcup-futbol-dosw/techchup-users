package edu.dosw.users.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Punto de entrada invocado cuando se requiere autenticación pero esta falta o es inválida.
 *
 * <p>Retorna un payload JSON estructurado con estado HTTP 401 para informar al cliente
 * que la autenticación falló o está ausente.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    /**
     * Envía una respuesta 401 Unauthorized con un cuerpo JSON que describe el error.
     * Las claves JSON reflejan el formato utilizado por {@code AccessDeniedHandlerImpl}
     * para mantener consistencia en las respuestas de error de la API.
     *
     * @param request       solicitud HTTP actual
     * @param response      respuesta HTTP actual donde se escribirá el payload JSON
     * @param authException excepción de autenticación lanzada por la capa de seguridad
     * @throws IOException      si falla la escritura de la respuesta
     * @throws ServletException presente para conformar con la firma de la interfaz
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException, ServletException {

        // Set status and content type
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Build error body
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", 401);
        body.put("error", "Unauthorized");
        body.put("message", "Invalid, expired, or missing token");
        body.put("path", request.getRequestURI());

        // Serialize and write
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}

