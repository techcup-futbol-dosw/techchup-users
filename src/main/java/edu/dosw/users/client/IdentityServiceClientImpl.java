package edu.dosw.users.client;

import edu.dosw.users.model.UserModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

/**
 * HTTP implementation of {@link IdentityServiceClient}.
 *
 * <p>Active only in the {@code prod} profile. In other profiles the in-memory
 * stub registered in {@code FallbackBeansConfig} is used instead.</p>
 */
@Service
@Profile("prod")
public class IdentityServiceClientImpl implements IdentityServiceClient {

    private final String identityServiceUrl;
    private final String usersBasePath;
    private final RestTemplate restTemplate;

    @Autowired
    public IdentityServiceClientImpl(
            @Value("${identity.service.url}") String identityServiceUrl,
            @Value("${identity.service.users-path:/api/users}") String usersBasePath) {
        this(identityServiceUrl, usersBasePath, new RestTemplate());
    }

    /** Package-private constructor used in unit tests to inject a mock RestTemplate. */
    IdentityServiceClientImpl(String identityServiceUrl, String usersBasePath, RestTemplate restTemplate) {
        this.identityServiceUrl = identityServiceUrl;
        this.usersBasePath = usersBasePath;
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean userExists(Long id) {
        try {
            restTemplate.getForEntity(identityServiceUrl + usersBasePath + "/" + id, Void.class);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    @Override
    public UserModel getUserById(Long id) {
        try {
            return restTemplate.getForObject(identityServiceUrl + usersBasePath + "/" + id, UserModel.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    @Override
    public UserModel getUserByIdentification(String identification) {
        URI uri = UriComponentsBuilder
                .fromUriString(identityServiceUrl + usersBasePath + "/identification/{id}")
                .buildAndExpand(identification)
                .toUri();
        try {
            return restTemplate.getForObject(uri, UserModel.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    @Override
    public List<UserModel> getAllUsers() {
        ResponseEntity<List<UserModel>> response = restTemplate.exchange(
                identityServiceUrl + usersBasePath,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});
        return response.getBody() != null ? response.getBody() : List.of();
    }

    @Override
    public UserModel createUser(UserModel model) {
        return restTemplate.postForObject(
                identityServiceUrl + usersBasePath,
                model,
                UserModel.class);
    }

    @Override
    public UserModel updateUser(Long id, UserModel model) {
        HttpEntity<UserModel> entity = new HttpEntity<>(model);
        return restTemplate.exchange(
                identityServiceUrl + usersBasePath + "/" + id,
                HttpMethod.PUT,
                entity,
                UserModel.class).getBody();
    }

    @Override
    public UserModel updateUserProfile(Long userId, UserModel model) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", userId.toString());
        HttpEntity<UserModel> entity = new HttpEntity<>(model, headers);
        return restTemplate.exchange(
                identityServiceUrl + usersBasePath + "/me",
                HttpMethod.PUT,
                entity,
                UserModel.class).getBody();
    }

    @Override
    public void deactivateUser(Long id) {
        restTemplate.patchForObject(
                identityServiceUrl + usersBasePath + "/" + id + "/deactivate",
                null,
                Void.class);
    }

    @Override
    public void inactivateUser(Long id) {
        restTemplate.patchForObject(
                identityServiceUrl + usersBasePath + "/" + id + "/inactivate",
                null,
                Void.class);
    }

    @Override
    public List<UserModel> searchUsers(String name, String status) {
        UriComponentsBuilder uri = UriComponentsBuilder
                .fromUriString(identityServiceUrl + usersBasePath + "/search");
        if (name != null) uri.queryParam("name", name);
        if (status != null) uri.queryParam("status", status);

        ResponseEntity<List<UserModel>> response = restTemplate.exchange(
                uri.toUriString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});
        return response.getBody() != null ? response.getBody() : List.of();
    }
}
