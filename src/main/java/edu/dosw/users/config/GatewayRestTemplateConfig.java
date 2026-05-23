package edu.dosw.users.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/**
 * Configura el {@link RestTemplate} utilizado para comunicarse con el API Gateway.
 *
 * <p>El bean creado aquí incluye un interceptor que reenvía el header
 * {@code Authorization} del request HTTP entrante a todas las llamadas salientes,
 * de modo que el JWT del usuario llega al Identity Service sin necesidad de
 * lógica adicional en cada cliente.</p>
 *
 * <p>Este bean solo se crea cuando la propiedad {@code gateway.url} está
 * configurada (entorno prod/Azure).</p>
 */
@Configuration
@ConditionalOnProperty(name = "gateway.url")
public class GatewayRestTemplateConfig {

    @Bean
    public RestTemplate gatewayRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(List.of(jwtForwardingInterceptor()));
        return restTemplate;
    }

    /**
     * Interceptor que propaga el header {@code Authorization} del contexto del
     * servlet actual a cada request saliente del {@code gatewayRestTemplate}.
     *
     * <p>Si no hay un contexto de servlet activo (p. ej. llamada desde un hilo
     * secundario), el header simplemente no se agrega y la llamada sigue sin él.</p>
     */
    private ClientHttpRequestInterceptor jwtForwardingInterceptor() {
        return (request, body, execution) -> {
            try {
                ServletRequestAttributes attributes =
                        (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
                HttpServletRequest servletRequest = attributes.getRequest();
                String authHeader = servletRequest.getHeader(HttpHeaders.AUTHORIZATION);
                if (authHeader != null) {
                    request.getHeaders().set(HttpHeaders.AUTHORIZATION, authHeader);
                }
            } catch (IllegalStateException ignored) {
                // Sin contexto de servlet activo — no se reenvía el header
            }
            return execution.execute(request, body);
        };
    }
}
