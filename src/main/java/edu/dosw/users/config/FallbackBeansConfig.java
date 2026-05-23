package edu.dosw.users.config;

import edu.dosw.users.client.IdentityServiceClient;
import edu.dosw.users.client.TeamsServiceClient;
import edu.dosw.users.dto.AccountDto;
import edu.dosw.users.model.PlayerPhoto;
import edu.dosw.users.service.ImageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Proporciona implementaciones de respaldo (no-op / stub) para beans
 * que están ausentes en entornos locales y de prueba.
 *
 * <ul>
 *   <li>Stub de {@link ImageService} — activo en todos los perfiles excepto {@code prod}.</li>
 *   <li>Stub de {@link TeamsServiceClient} — activo hasta que se registre un cliente HTTP real.</li>
 * </ul>
 */
@Configuration
public class FallbackBeansConfig {

    /**
     * Stub no-op de {@link ImageService}: {@code upload} retorna {@code null},
     * {@code getPhoto} retorna {@code null} y {@code delete} no hace nada.
     * Activo en todos los perfiles excepto {@code prod}.
     */
    @Bean
    @Profile("!prod")
    public ImageService imageServiceStub() {
        return new ImageService() {
            @Override
            public String upload(MultipartFile file, Long sportProfileId) {
                return null;
            }

            @Override
            public PlayerPhoto getPhoto(String photoId) {
                return null;
            }

            @Override
            public void delete(String photoId) {
                // no-op
            }
        };
    }

    /**
     * Stub de {@link TeamsServiceClient}: siempre retorna {@code false} (el jugador
     * no pertenece a ningún equipo). Activo hasta que se registre un cliente HTTP real.
     */
    @Bean
    @ConditionalOnMissingBean(TeamsServiceClient.class)
    public TeamsServiceClient teamsServiceClientStub() {
        return userId -> false;
    }

    /**
     * Stub de {@link IdentityServiceClient}: retorna {@code null} para consultas
     * individuales y lista vacía para consultas de lista. Activo cuando la propiedad
     * {@code gateway.url} no está configurada (entorno local y pruebas).
     */
    @Bean
    @ConditionalOnMissingBean(IdentityServiceClient.class)
    public IdentityServiceClient identityServiceClientStub() {
        return new IdentityServiceClient() {
            @Override
            public AccountDto getAccountById(Long id) {
                return null;
            }

            @Override
            public List<AccountDto> getAllAccounts() {
                return List.of();
            }
        };
    }
}
