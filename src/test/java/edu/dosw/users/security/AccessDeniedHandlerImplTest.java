package edu.dosw.users.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AccessDeniedHandlerImpl.
 *
 * <p>This test class validates the access denied exception handler including:
 * <ul>
 *   <li><b>HTTP Response Status</b>: Verifies 403 Forbidden status code</li>
 *   <li><b>Content Type</b>: Verifies JSON content type</li>
 *   <li><b>Response Body</b>: Validates JSON response structure and fields</li>
 *   <li><b>Request Information</b>: Ensures request path is included in response</li>
 * </ul>
 *
 * <p><b>Approach</b>: Uses Mockito to mock HTTP request/response and ByteArrayOutputStream
 * to capture and deserialize the JSON response body.
 *
 * @see AccessDeniedHandlerImpl
 */
@ExtendWith(MockitoExtension.class)
class AccessDeniedHandlerImplTest {

    private AccessDeniedHandlerImpl handler;
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    /**
     * Setup method executed before each test.
     * Initializes the handler and ObjectMapper for JSON deserialization.
     */
    @BeforeEach
    void setUp() {
        handler = new AccessDeniedHandlerImpl();
        objectMapper = new ObjectMapper();
    }

    /**
     * Validates that the response status code is 403 Forbidden.
     * 
     * <p><b>Scenario</b>: Access denied exception is handled.
     * 
     * <p><b>Expected Result</b>: Response status is set to 403.
     */
    @Test
    @DisplayName("Response status code should be 403 Forbidden")
    void testHandle_ResponseStatusIs403() throws IOException, jakarta.servlet.ServletException {
        // Arrange
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new MockServletOutputStream(outputStream));
        when(request.getRequestURI()).thenReturn("/api/users");
        AccessDeniedException exception = new AccessDeniedException("Access Denied");

        // Act
        handler.handle(request, response, exception);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    /**
     * Validates that the response content type is set to application/json.
     * 
     * <p><b>Scenario</b>: Access denied exception is handled.
     * 
     * <p><b>Expected Result</b>: Response content type is "application/json".
     */
    @Test
    @DisplayName("Response content type should be application/json")
    void testHandle_ContentTypeIsJson() throws IOException, jakarta.servlet.ServletException {
        // Arrange
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new MockServletOutputStream(outputStream));
        when(request.getRequestURI()).thenReturn("/api/users");
        AccessDeniedException exception = new AccessDeniedException("Access Denied");

        // Act
        handler.handle(request, response, exception);

        // Assert
        verify(response).setContentType("application/json");
    }

    /**
     * Validates that response body contains required fields (status, error, message).
     * 
     * <p><b>Scenario</b>: Access denied exception is handled.
     * 
     * <p><b>Expected Result</b>: Response JSON contains status (403), error label, and message.
     */
    @Test
    @DisplayName("Response body contains required fields")
    void testHandle_ResponseBodyContainsRequiredFields() throws IOException, jakarta.servlet.ServletException {
        // Arrange
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new MockServletOutputStream(outputStream));
        when(request.getRequestURI()).thenReturn("/api/users");
        AccessDeniedException exception = new AccessDeniedException("Access Denied");

        // Act
        handler.handle(request, response, exception);

        // Assert: Parse response
        String jsonResponse = outputStream.toString();
        Map<String, Object> body = objectMapper.readValue(jsonResponse, Map.class);
        
        assertEquals(403, body.get("status"), "Status should be 403");
        assertEquals("Forbidden", body.get("error"), "Error should be 'Forbidden'");
        assertNotNull(body.get("message"), "Message should be present");
        assertNotNull(body.get("timestamp"), "Timestamp should be present");
    }

    /**
     * Validates that response includes the request path.
     * 
     * <p><b>Scenario</b>: Access denied for a specific endpoint.
     * 
     * <p><b>Expected Result</b>: Response path field matches request URI.
     */
    @Test
    @DisplayName("Response path field matches request URI")
    void testHandle_ResponsePathMatchesRequestUri() throws IOException, jakarta.servlet.ServletException {
        // Arrange
        String requestUri = "/api/admin/users/123";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new MockServletOutputStream(outputStream));
        when(request.getRequestURI()).thenReturn(requestUri);
        AccessDeniedException exception = new AccessDeniedException("Access Denied");

        // Act
        handler.handle(request, response, exception);

        // Assert: Parse response
        String jsonResponse = outputStream.toString();
        Map<String, Object> body = objectMapper.readValue(jsonResponse, Map.class);
        
        assertEquals(requestUri, body.get("path"), "Path should match request URI");
    }

    /**
     * Validates that response message indicates permission issue.
     * 
     * <p><b>Scenario</b>: Access denied exception is handled.
     * 
     * <p><b>Expected Result</b>: Response message mentions "permission".
     */
    @Test
    @DisplayName("Response message indicates permission issue")
    void testHandle_MessageIndicatesPermission() throws IOException, jakarta.servlet.ServletException {
        // Arrange
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new MockServletOutputStream(outputStream));
        when(request.getRequestURI()).thenReturn("/api/users");
        AccessDeniedException exception = new AccessDeniedException("Access Denied");

        // Act
        handler.handle(request, response, exception);

        // Assert: Parse response
        String jsonResponse = outputStream.toString();
        Map<String, Object> body = objectMapper.readValue(jsonResponse, Map.class);
        String message = (String) body.get("message");
        
        assertTrue(message.contains("permission") || message.contains("access"), 
                "Message should indicate permission issue");
    }

    /**
     * Mock implementation of ServletOutputStream for testing.
     */
    private static class MockServletOutputStream extends jakarta.servlet.ServletOutputStream {
        private final ByteArrayOutputStream output;

        MockServletOutputStream(ByteArrayOutputStream output) {
            this.output = output;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(jakarta.servlet.WriteListener listener) {
            // Mock implementation: no-op for testing purposes
        }

        @Override
        public void write(int b) {
            output.write(b);
        }
    }
}

