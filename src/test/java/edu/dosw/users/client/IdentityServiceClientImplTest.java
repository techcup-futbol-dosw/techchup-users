package edu.dosw.users.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.dosw.users.model.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

/**
 * Unit tests for {@link IdentityServiceClientImpl}.
 *
 * <p>Uses {@link MockRestServiceServer} to intercept HTTP calls and verify
 * request construction without a running identity service.</p>
 */
class IdentityServiceClientImplTest {

    private static final String BASE_URL = "http://localhost:8081";

    private MockRestServiceServer server;
    private IdentityServiceClientImpl client;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.createServer(restTemplate);
        client = new IdentityServiceClientImpl(BASE_URL, restTemplate);
    }

    // ── userExists ────────────────────────────────────────────────────────────

    @Test
    void userExists_returnsTrueWhenUserFound() {
        server.expect(requestTo(BASE_URL + "/api/users/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess());

        assertTrue(client.userExists(1L));
        server.verify();
    }

    @Test
    void userExists_returnsFalseWhenNotFound() {
        server.expect(requestTo(BASE_URL + "/api/users/9999"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertFalse(client.userExists(9999L));
        server.verify();
    }

    // ── getUserById ───────────────────────────────────────────────────────────

    @Test
    void getUserById_returnsUserWhenFound() throws Exception {
        UserModel expected = UserModel.builder().id(1L).fullName("Carlos").build();
        server.expect(requestTo(BASE_URL + "/api/users/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expected),
                        MediaType.APPLICATION_JSON));

        UserModel result = client.getUserById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Carlos", result.getFullName());
        server.verify();
    }

    @Test
    void getUserById_returnsNullWhenNotFound() {
        server.expect(requestTo(BASE_URL + "/api/users/9999"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertNull(client.getUserById(9999L));
        server.verify();
    }

    // ── getUserByIdentification ───────────────────────────────────────────────

    @Test
    void getUserByIdentification_returnsUserWhenFound() throws Exception {
        UserModel expected = UserModel.builder().id(1L).identification("12345678").build();
        server.expect(requestTo(BASE_URL + "/api/users/identification/12345678"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expected),
                        MediaType.APPLICATION_JSON));

        UserModel result = client.getUserByIdentification("12345678");
        assertNotNull(result);
        assertEquals("12345678", result.getIdentification());
        server.verify();
    }

    @Test
    void getUserByIdentification_returnsNullWhenNotFound() {
        server.expect(requestTo(BASE_URL + "/api/users/identification/NOEXISTE"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertNull(client.getUserByIdentification("NOEXISTE"));
        server.verify();
    }

    // ── getAllUsers ───────────────────────────────────────────────────────────

    @Test
    void getAllUsers_returnsList() throws Exception {
        List<UserModel> users = List.of(
                UserModel.builder().id(1L).build(),
                UserModel.builder().id(2L).build());
        server.expect(requestTo(BASE_URL + "/api/users"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(users),
                        MediaType.APPLICATION_JSON));

        List<UserModel> result = client.getAllUsers();
        assertEquals(2, result.size());
        server.verify();
    }

    // ── createUser ────────────────────────────────────────────────────────────

    @Test
    void createUser_returnsCreatedUser() throws Exception {
        UserModel input = UserModel.builder().fullName("Nuevo Usuario").build();
        UserModel expected = UserModel.builder().id(1L).fullName("Nuevo Usuario").build();
        server.expect(requestTo(BASE_URL + "/api/users"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expected),
                        MediaType.APPLICATION_JSON));

        UserModel result = client.createUser(input);
        assertNotNull(result);
        assertEquals(1L, result.getId());
        server.verify();
    }

    // ── updateUser ────────────────────────────────────────────────────────────

    @Test
    void updateUser_returnsUpdatedUser() throws Exception {
        UserModel input = UserModel.builder().fullName("Nombre Nuevo").build();
        UserModel expected = UserModel.builder().id(1L).fullName("Nombre Nuevo").build();
        server.expect(requestTo(BASE_URL + "/api/users/1"))
                .andExpect(method(HttpMethod.PUT))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expected),
                        MediaType.APPLICATION_JSON));

        UserModel result = client.updateUser(1L, input);
        assertNotNull(result);
        assertEquals("Nombre Nuevo", result.getFullName());
        server.verify();
    }

    // ── updateUserProfile ─────────────────────────────────────────────────────

    @Test
    void updateUserProfile_sendsXUserIdHeaderAndReturnsModel() throws Exception {
        UserModel input = UserModel.builder().fullName("Perfil Nuevo").build();
        UserModel expected = UserModel.builder().id(1L).fullName("Perfil Nuevo").build();
        server.expect(requestTo(BASE_URL + "/api/users/me"))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(header("X-User-Id", "1"))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expected),
                        MediaType.APPLICATION_JSON));

        UserModel result = client.updateUserProfile(1L, input);
        assertNotNull(result);
        assertEquals("Perfil Nuevo", result.getFullName());
        server.verify();
    }

    // ── deactivateUser ────────────────────────────────────────────────────────

    @Test
    void deactivateUser_sendsCorrectRequest() {
        server.expect(requestTo(BASE_URL + "/api/users/1/deactivate"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withSuccess());

        assertDoesNotThrow(() -> client.deactivateUser(1L));
        server.verify();
    }

    // ── inactivateUser ────────────────────────────────────────────────────────

    @Test
    void inactivateUser_sendsCorrectRequest() {
        server.expect(requestTo(BASE_URL + "/api/users/1/inactivate"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withSuccess());

        assertDoesNotThrow(() -> client.inactivateUser(1L));
        server.verify();
    }

    // ── searchUsers ───────────────────────────────────────────────────────────

    @Test
    void searchUsers_withBothParams_includesQueryParams() throws Exception {
        List<UserModel> users = List.of(UserModel.builder().id(1L).build());
        server.expect(requestTo(containsString("/api/users/search")))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(users),
                        MediaType.APPLICATION_JSON));

        List<UserModel> result = client.searchUsers("Juan", "ACTIVE");
        assertEquals(1, result.size());
        server.verify();
    }

    @Test
    void searchUsers_withNullParams_omitsQueryParams() throws Exception {
        List<UserModel> users = List.of(
                UserModel.builder().id(1L).build(),
                UserModel.builder().id(2L).build());
        server.expect(requestTo(BASE_URL + "/api/users/search"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(users),
                        MediaType.APPLICATION_JSON));

        List<UserModel> result = client.searchUsers(null, null);
        assertEquals(2, result.size());
        server.verify();
    }
}