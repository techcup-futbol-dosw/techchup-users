package edu.dosw.users.config;

import edu.dosw.users.client.IdentityServiceClient;
import edu.dosw.users.client.TeamsServiceClient;
import edu.dosw.users.exception.ResourceNotFoundException;
import edu.dosw.users.model.PlayerPhoto;
import edu.dosw.users.model.UserModel;
import edu.dosw.users.service.ImageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Provides fallback (no-op / in-memory) implementations for beans that are
 * absent in local and test environments.
 *
 * <ul>
 *   <li>{@link ImageService} stub — active in all profiles except {@code prod}.</li>
 *   <li>{@link IdentityServiceClient} stub — active in all profiles except {@code prod};
 *       keeps an in-memory store so local development works end-to-end.</li>
 *   <li>{@link TeamsServiceClient} stub — active until a real HTTP client is registered.</li>
 * </ul>
 */
@Configuration
public class FallbackBeansConfig {

    /**
     * No-op {@link ImageService}: upload returns {@code null}, getPhoto returns
     * {@code null}, delete is a no-op. Active in all profiles except {@code prod}.
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
     * In-memory {@link IdentityServiceClient} stub. Active in all profiles except
     * {@code prod}. Stores users in a thread-safe map so that local development
     * and integration tests can exercise the full request flow without a running
     * identity service.
     */
    @Bean
    @Profile("!prod")
    public IdentityServiceClient identityServiceClientStub() {
        return new IdentityServiceClient() {
            private final ConcurrentHashMap<Long, UserModel> store = new ConcurrentHashMap<>();
            private final ConcurrentHashMap<String, Long> byIdentification = new ConcurrentHashMap<>();
            private final AtomicLong idGen = new AtomicLong(1);

            @Override
            public boolean userExists(Long id) {
                return store.containsKey(id);
            }

            @Override
            public UserModel getUserById(Long id) {
                return store.get(id);
            }

            @Override
            public UserModel getUserByIdentification(String identification) {
                Long id = byIdentification.get(identification);
                return id != null ? store.get(id) : null;
            }

            @Override
            public List<UserModel> getAllUsers() {
                return new ArrayList<>(store.values());
            }

            @Override
            public UserModel createUser(UserModel model) {
                Long id = idGen.getAndIncrement();
                model.setId(id);
                if (model.getStatus() == null) model.setStatus("ACTIVE");
                if (model.getProfileCreatedAt() == null) model.setProfileCreatedAt(LocalDateTime.now());
                if (model.getUpdatedAt() == null) model.setUpdatedAt(LocalDateTime.now());
                store.put(id, model);
                if (model.getIdentification() != null) {
                    byIdentification.put(model.getIdentification(), id);
                }
                return model;
            }

            @Override
            public UserModel updateUser(Long id, UserModel model) {
                UserModel existing = store.get(id);
                if (existing == null) {
                    throw new ResourceNotFoundException("User not found with id: " + id);
                }
                if (model.getFullName() != null) existing.setFullName(model.getFullName());
                if (model.getSchoolRelation() != null) existing.setSchoolRelation(model.getSchoolRelation());
                if (model.getAcademicProgram() != null) existing.setAcademicProgram(model.getAcademicProgram());
                if (model.getSemester() != null) existing.setSemester(model.getSemester());
                existing.setUpdatedAt(LocalDateTime.now());
                return existing;
            }

            @Override
            public UserModel updateUserProfile(Long userId, UserModel model) {
                return updateUser(userId, model);
            }

            @Override
            public void deactivateUser(Long id) {
                UserModel existing = store.get(id);
                if (existing == null) {
                    throw new ResourceNotFoundException("User not found with id: " + id);
                }
                existing.setStatus("INACTIVE");
                existing.setUpdatedAt(LocalDateTime.now());
            }

            @Override
            public void inactivateUser(Long id) {
                deactivateUser(id);
            }

            @Override
            public List<UserModel> searchUsers(String name, String status) {
                return store.values().stream()
                        .filter(u -> name == null || (u.getFullName() != null
                                && u.getFullName().toLowerCase().contains(name.toLowerCase())))
                        .filter(u -> status == null || status.equalsIgnoreCase(u.getStatus()))
                        .collect(Collectors.toList());
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
