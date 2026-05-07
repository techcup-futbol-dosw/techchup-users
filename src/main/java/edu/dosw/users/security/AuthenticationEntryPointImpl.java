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
 * Entry point invoked when authentication is required but missing or invalid.
 *
 * <p>Returns a structured JSON payload with HTTP 401 status to inform the
 * client that authentication failed or is absent.</p>
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    /**
     * Sends a 401 Unauthorized response with a JSON body describing the error.
     * The JSON keys mirror the format used by {@code AccessDeniedHandlerImpl}
     * to keep API error responses consistent.
     *
     * @param request current HTTP request
     * @param response current HTTP response where the JSON payload will be written
     * @param authException the authentication exception thrown by the security layer
     * @throws IOException when writing the response fails
     * @throws ServletException present to conform with the interface signature
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

