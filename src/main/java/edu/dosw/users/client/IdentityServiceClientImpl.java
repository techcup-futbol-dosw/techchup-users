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

    private static final String USERS_PATH = "/api/users/";
    private static final String USERS_ENDPOINT = "/api/users";

    private final String identityServiceUrl;
    private final RestTemplate restTemplate;

    @Autowired
    public IdentityServiceClientImpl(
            @Value("${identity.service.url}") String identityServiceUrl) {
        this(identityServiceUrl, new RestTemplate());
    }

    /** Package-private constructor used in unit tests to inject a mock RestTemplate. */
    IdentityServiceClientImpl(String identityServiceUrl, RestTemplate restTemplate) {
        this.identityServiceUrl = identityServiceUrl;
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean userExists(Long id) {
        try {
            restTemplate.getForEntity(identityServiceUrl + USERS_PATH + id, Void.class);
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    @Override
    public UserModel getUserById(Long id) {
        try {
            return restTemplate.getForObject(identityServiceUrl + USERS_PATH + id, UserModel.class);
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    @Override
    public UserModel getUserByIdentification(String identification) {
        URI uri = UriComponentsBuilder
                .fromUriString(identityServiceUrl + USERS_ENDPOINT + "/identification/{id}")
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
                identityServiceUrl + USERS_ENDPOINT,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});
        return response.getBody() != null ? response.getBody() : List.of();
    }

    @Override
    public UserModel createUser(UserModel model) {
        return restTemplate.postForObject(
                identityServiceUrl + USERS_ENDPOINT,
                model,
                UserModel.class);
    }

    @Override
    public UserModel updateUser(Long id, UserModel model) {
        HttpEntity<UserModel> entity = new HttpEntity<>(model);
        return restTemplate.exchange(
                identityServiceUrl + USERS_PATH + id,
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
                identityServiceUrl + USERS_ENDPOINT + "/me",
                HttpMethod.PUT,
                entity,
                UserModel.class).getBody();
    }

    @Override
    public void deactivateUser(Long id) {
        restTemplate.patchForObject(
                identityServiceUrl + USERS_PATH + id + "/deactivate",
                null,
                Void.class);
    }

    @Override
    public void inactivateUser(Long id) {
        restTemplate.patchForObject(
                identityServiceUrl + USERS_PATH + id + "/inactivate",
                null,
                Void.class);
    }

    @Override
    public List<UserModel> searchUsers(String name, String status) {
        UriComponentsBuilder uri = UriComponentsBuilder
                .fromUriString(identityServiceUrl + USERS_ENDPOINT + "/search");
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
