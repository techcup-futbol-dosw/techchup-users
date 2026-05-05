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
 * Handler invoked when an authenticated principal attempts to access a
 * resource for which they lack sufficient privileges.
 *
 * <p>Produces a concise JSON error response with fields:
 * <ul>
 *   <li>timestamp - ISO timestamp of the error</li>
 *   <li>status - HTTP status code (403)</li>
 *   <li>error - short error phrase</li>
 *   <li>message - human readable message</li>
 *   <li>path - request URI</li>
 * </ul>
 * This class centralizes the format so all access-denied responses are
 * consistent across the service.
 */
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {

    /**
     * Handles an AccessDeniedException by writing a structured JSON body and
     * setting the response status to 403 (Forbidden).
     *
     * @param request current HTTP request
     * @param response current HTTP response (written with JSON payload)
     * @param accessDeniedException the exception raised by the security framework
     * @throws IOException when writing the response fails
     * @throws ServletException never thrown in current implementation but kept
     *                          to satisfy the interface contract
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

