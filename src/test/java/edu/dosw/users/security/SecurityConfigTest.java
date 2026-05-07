package edu.dosw.users.security;

import edu.dosw.users.service.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.TestPropertySource;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Integration tests for {@link SecurityConfig}.
 *
 * <p>These tests execute the real Spring Security filter chain so the configuration bean is
 * created during application startup and its HTTP rules are covered by actual requests.</p>
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
class SecurityConfigTest {

    @Value("${security.jwt.secret}")
    private String testSecret;

    @Autowired
    private FilterChainProxy filterChainProxy;

    @Autowired
    private SecurityConfig securityConfig;

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("Application context loads the security beans")
    void contextLoadsSecurityBeans() {
        assertNotNull(securityConfig);
        assertNotNull(jwtService);
        assertNotNull(filterChainProxy);
    }

    @Test
    @DisplayName("JWT filter is registered in the security chain")
    void jwtFilterIsRegisteredInTheSecurityChain() {
        List<jakarta.servlet.Filter> filters = filterChainProxy.getFilters("/api/users");

        int jwtIndex = indexOf(filters, JwtAuthenticationFilter.class);
        assertTrue(jwtIndex >= 0, "JWT filter should be present in the chain");
    }

     @Test
     @DisplayName("Unauthenticated requests are rejected by the security filter chain")
     void unauthenticatedRequestIsRejectedBySecurityFilterChain() throws ServletException, IOException {
         MockHttpServletRequest request = buildRequest();
         MockHttpServletResponse response = new MockHttpServletResponse();
         FilterChain downstreamChain = mock(FilterChain.class);

         filterChainProxy.doFilter(request, response, downstreamChain);

         assertEquals(401, response.getStatus());
         verify(downstreamChain, never()).doFilter(any(), any());
     }

     @Test
     @DisplayName("Valid Bearer tokens are allowed through the security filter chain")
     void validBearerTokenIsAllowedThroughSecurityFilterChain() throws ServletException, IOException {
         MockHttpServletRequest request = buildRequest();
         request.addHeader("Authorization", "Bearer " + createAccessToken("123"));

         MockHttpServletResponse response = new MockHttpServletResponse();
         FilterChain downstreamChain = mock(FilterChain.class);

         filterChainProxy.doFilter(request, response, downstreamChain);

         assertEquals(200, response.getStatus());
         verify(downstreamChain).doFilter(any(), any());
     }

     private static MockHttpServletRequest buildRequest() {
         MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/security-config-test");
         request.setServletPath("/api/security-config-test");
         request.setRequestURI("/api/security-config-test");
         return request;
     }

     private static int indexOf(List<jakarta.servlet.Filter> filters, Class<?> type) {
         for (int i = 0; i < filters.size(); i++) {
             if (type.isInstance(filters.get(i))) {
                 return i;
             }
         }

         return -1;
     }

     private String createAccessToken(String subject) {
         SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(testSecret));

         return Jwts.builder()
                 .subject(subject)
                 .claim("tokenType", "ACCESS")
                 .claim("roles", List.of("ADMIN"))
                 .claim("permissions", List.of("read:users"))
                 .issuedAt(Date.from(Instant.now()))
                 .expiration(Date.from(Instant.now().plusSeconds(3600)))
                 .signWith(key)
                 .compact();
     }
}
