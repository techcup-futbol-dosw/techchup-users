package edu.dosw.users.client;

import edu.dosw.users.dto.AccountDto;
import edu.dosw.users.dto.AccountPageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * Implementación real de {@link IdentityServiceClient}.
 *
 * <p>Activa solo cuando la propiedad {@code gateway.url} está configurada
 * (variable de entorno {@code GATEWAY_URL} en Azure). En entornos locales y de
 * test, {@code FallbackBeansConfig} registra el stub correspondiente.</p>
 *
 * <p>El {@code gatewayRestTemplate} inyectado incluye un interceptor que
 * reenvía automáticamente el header {@code Authorization} del request HTTP
 * entrante a cada llamada saliente al API Gateway.</p>
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "gateway.url")
public class IdentityServiceClientImpl implements IdentityServiceClient {

    @Value("${gateway.url}")
    private String gatewayUrl;

    private final RestTemplate gatewayRestTemplate;

    public IdentityServiceClientImpl(RestTemplate gatewayRestTemplate) {
        this.gatewayRestTemplate = gatewayRestTemplate;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Llama a {@code GET /accounts/{id}} en el Identity Service. Retorna
     * {@code null} si la cuenta no existe (404) o si ocurre cualquier error de
     * comunicación.</p>
     */
    @Override
    public AccountDto getAccountById(Long id) {
        try {
            ResponseEntity<AccountDto> response = gatewayRestTemplate.getForEntity(
                    gatewayUrl + "/accounts/" + id, AccountDto.class);
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (Exception e) {
            log.warn("No se pudo obtener la cuenta {} del Identity Service: {}", id, e.getMessage());
            return null;
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Llama a {@code GET /accounts?page=0&size=500}. Requiere rol ADMIN en
     * el JWT del request actual. Si el token no tiene el permiso necesario,
     * Identity responde 403 y este método retorna lista vacía.</p>
     */
    @Override
    public List<AccountDto> getAllAccounts() {
        try {
            ResponseEntity<AccountPageDto> response = gatewayRestTemplate.getForEntity(
                    gatewayUrl + "/accounts?page=0&size=500", AccountPageDto.class);
            AccountPageDto page = response.getBody();
            return (page != null && page.getContent() != null) ? page.getContent() : List.of();
        } catch (Exception e) {
            log.warn("No se pudo obtener la lista de cuentas del Identity Service: {}", e.getMessage());
            return List.of();
        }
    }
}
