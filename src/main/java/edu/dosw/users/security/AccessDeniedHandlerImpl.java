package edu.dosw.users.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador invocado cuando un principal autenticado intenta acceder a un recurso
 * para el que no tiene privilegios suficientes.
 *
 * <p>Produce una respuesta de error JSON concisa con los campos:
 * <ul>
 *   <li>{@code timestamp} — marca de tiempo ISO del error</li>
 *   <li>{@code status} — código de estado HTTP (403)</li>
 *   <li>{@code error} — frase corta del error</li>
 *   <li>{@code message} — mensaje legible por humanos</li>
 *   <li>{@code path} — URI de la solicitud</li>
 * </ul>
 * Esta clase centraliza el formato para que todas las respuestas de acceso denegado
 * sean consistentes en todo el servicio.
 *
 * @author CodeForge
 * @since 1.0
 */
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    /**
     * Maneja una {@link AccessDeniedException} escribiendo un cuerpo JSON estructurado
     * y estableciendo el estado de la respuesta en 403 (Forbidden).
     *
     * @param request               solicitud HTTP actual
     * @param response              respuesta HTTP actual donde se escribe el payload JSON
     * @param accessDeniedException excepción lanzada por el framework de seguridad
     * @throws IOException      si falla la escritura de la respuesta
     * @throws ServletException nunca lanzada en la implementación actual, pero presente
     *                          para satisfacer el contrato de la interfaz
     */
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException)
            throws IOException, ServletException {

        // Set HTTP status and JSON content type
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        // Build a predictable JSON body to return to the client
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", 403);
        body.put("error", "Forbidden");
        body.put("message", "You do not have permission to access this resource");
        body.put("path", request.getRequestURI());

        // Use Jackson ObjectMapper to write the JSON payload
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}

