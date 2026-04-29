package edu.dosw.users.config;

import edu.dosw.users.client.TeamsServiceClient;
import edu.dosw.users.service.ImageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.multipart.MultipartFile;

/**
 * Provides fallback (no-op) implementations for beans that are absent in
 * local and test environments.
 *
 * <ul>
 *   <li>{@link ImageService} stub — active when {@code MongoTemplate} is not
 *       in the context (MongoDB excluded in tests). Mirrors the same condition
 *       used by {@code ImageServiceImpl} so the two never conflict.</li>
 *   <li>{@link TeamsServiceClient} stub — active until a real HTTP client
 *       implementation is registered.</li>
 * </ul>
 */
@Configuration
public class FallbackBeansConfig {

    /**
     * No-op {@link ImageService}: upload returns {@code null}, delete is a
     * no-op. Active only when {@code MongoTemplate} is not present.
     */
    @Bean
    @ConditionalOnMissingBean(MongoTemplate.class)
    public ImageService imageServiceStub() {
        return new ImageService() {
            @Override
            public String upload(MultipartFile file, Long sportProfileId) {
                return null;
            }

            @Override
            public void delete(String photoId) {
                // no-op
            }
        };
    }

    /**
     * Stub {@link TeamsServiceClient}: always returns {@code false} (player
     * not in any team). Active until a real HTTP client is registered.
     */
    @Bean
    @ConditionalOnMissingBean(TeamsServiceClient.class)
    public TeamsServiceClient teamsServiceClientStub() {
        return userId -> false;
    }
}