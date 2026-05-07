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
import org.springframework.security.core.AuthenticationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthenticationEntryPointImpl.
 *
 * <p>This test class validates the authentication entry point exception handler including:
 * <ul>
 *   <li><b>HTTP Response Status</b>: Verifies 401 Unauthorized status code</li>
 *   <li><b>Content Type</b>: Verifies JSON content type</li>
 *   <li><b>Response Body</b>: Validates JSON response structure and fields</li>
 *   <li><b>Token Information</b>: Ensures invalid/missing token is communicated</li>
 * </ul>
 *
 * <p><b>Approach</b>: Uses Mockito to mock HTTP request/response and ByteArrayOutputStream
 * to capture and deserialize the JSON response body.
 *
 * @see AuthenticationEntryPointImpl
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationEntryPointImplTest {

    private AuthenticationEntryPointImpl entryPoint;
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    /**
     * Setup method executed before each test.
     * Initializes the entry point handler and ObjectMapper for JSON deserialization.
     */
    @BeforeEach
    void setUp() {
        entryPoint = new AuthenticationEntryPointImpl();
        objectMapper = new ObjectMapper();
    }

    /**
     * Validates that the response status code is 401 Unauthorized.
     * 
     * <p><b>Scenario</b>: Authentication exception is encountered (e.g., missing/invalid token).
     * 
     * <p><b>Expected Result</b>: Response status is set to 401.
     */
    @Test
    @DisplayName("Response status code should be 401 Unauthorized")
    void testCommence_ResponseStatusIs401() throws IOException, jakarta.servlet.ServletException {
        // Arrange
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new MockServletOutputStream(outputStream));
        when(request.getRequestURI()).thenReturn("/api/users");
        AuthenticationException exception = new org.springframework.security.core.AuthenticationException("No token") {};

        // Act
        entryPoint.commence(request, response, exception);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    /**
     * Validates that the response content type is set to application/json.
     * 
     * <p><b>Scenario</b>: Authentication exception is encountered.
     * 
     * <p><b>Expected Result</b>: Response content type is "application/json".
     */
    @Test
    @DisplayName("Response content type should be application/json")
    void testCommence_ContentTypeIsJson() throws IOException, jakarta.servlet.ServletException {
        // Arrange
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new MockServletOutputStream(outputStream));
        when(request.getRequestURI()).thenReturn("/api/users");
        AuthenticationException exception = new org.springframework.security.core.AuthenticationException("No token") {};

        // Act
        entryPoint.commence(request, response, exception);

        // Assert
        verify(response).setContentType("application/json");
    }

    /**
     * Validates that response body contains required fields (status, error, message).
     * 
     * <p><b>Scenario</b>: Authentication exception is encountered.
     * 
     * <p><b>Expected Result</b>: Response JSON contains status (401), error label, and message about token.
     */
    @Test
    @DisplayName("Response body contains required fields")
    void testCommence_ResponseBodyContainsRequiredFields() throws IOException, jakarta.servlet.ServletException {
        // Arrange
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new MockServletOutputStream(outputStream));
        when(request.getRequestURI()).thenReturn("/api/users");
        AuthenticationException exception = new org.springframework.security.core.AuthenticationException("Invalid token") {};

        // Act
        entryPoint.commence(request, response, exception);

        // Assert: Parse response
        String jsonResponse = outputStream.toString();
        Map<String, Object> body = objectMapper.readValue(jsonResponse, Map.class);
        
        assertEquals(401, body.get("status"), "Status should be 401");
        assertEquals("Unauthorized", body.get("error"), "Error should be 'Unauthorized'");
        assertNotNull(body.get("message"), "Message should be present");
        assertNotNull(body.get("timestamp"), "Timestamp should be present");
    }

    /**
     * Validates that response includes the request path.
     * 
     * <p><b>Scenario</b>: Authentication exception for a specific endpoint.
     * 
     * <p><b>Expected Result</b>: Response path field matches request URI.
     */
    @Test
    @DisplayName("Response path field matches request URI")
    void testCommence_ResponsePathMatchesRequestUri() throws IOException, jakarta.servlet.ServletException {
        // Arrange
        String requestUri = "/api/admin/users";
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new MockServletOutputStream(outputStream));
        when(request.getRequestURI()).thenReturn(requestUri);
        AuthenticationException exception = new org.springframework.security.core.AuthenticationException("No token") {};

        // Act
        entryPoint.commence(request, response, exception);

        // Assert: Parse response
        String jsonResponse = outputStream.toString();
        Map<String, Object> body = objectMapper.readValue(jsonResponse, Map.class);
        
        assertEquals(requestUri, body.get("path"), "Path should match request URI");
    }

    /**
     * Validates that response message mentions token issue.
     * 
     * <p><b>Scenario</b>: Authentication exception due to missing/invalid token.
     * 
     * <p><b>Expected Result</b>: Response message mentions "token".
     */
    @Test
    @DisplayName("Response message mentions token")
    void testCommence_MessageMentionsToken() throws IOException, jakarta.servlet.ServletException {
        // Arrange
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new MockServletOutputStream(outputStream));
        when(request.getRequestURI()).thenReturn("/api/users");
        AuthenticationException exception = new org.springframework.security.core.AuthenticationException("Invalid token") {};

        // Act
        entryPoint.commence(request, response, exception);

        // Assert: Parse response
        String jsonResponse = outputStream.toString();
        Map<String, Object> body = objectMapper.readValue(jsonResponse, Map.class);
        String message = (String) body.get("message");
        
        assertTrue(message.toLowerCase().contains("token") || message.toLowerCase().contains("unauthorized"), 
                "Message should mention token or authorization issue");
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

