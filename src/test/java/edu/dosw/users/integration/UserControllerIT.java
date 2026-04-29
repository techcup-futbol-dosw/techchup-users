package edu.dosw.users.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.dosw.users.dto.UserRequest;
import edu.dosw.users.repository.AuditLogRepository;
import edu.dosw.users.repository.InvitationRepository;
import edu.dosw.users.repository.SportProfileRepository;
import edu.dosw.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@code UserController}.
 *
 * <p>Uses the full Spring context with a real H2 database and no mocked beans,
 * verifying that the Controller → Service → Repository chain works end-to-end.</p>
 */
@SpringBootTest
class UserControllerIT {

    @Autowired private WebApplicationContext context;
    @Autowired private UserRepository userRepository;
    @Autowired private SportProfileRepository sportProfileRepository;
    @Autowired private InvitationRepository invitationRepository;
    @Autowired private AuditLogRepository auditLogRepository;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    /**
     * Builds the {@link MockMvc} instance and clears persisted data so each
     * integration test starts from an isolated database state.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        auditLogRepository.deleteAll();
        invitationRepository.deleteAll();
        sportProfileRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ── POST /api/users ───────────────────────────────────────────────────────

    @Test
    void createUser_persistsAndReturns201() throws Exception {
        UserRequest request = UserRequest.builder()
                .fullName("Carlos Perez")
                .email("carlos@eci.edu.co")
                .password("hash123")
                .identification("11223344")
                .build();

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.fullName").value("Carlos Perez"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    // ── GET /api/users ────────────────────────────────────────────────────────

    @Test
    void getAll_returnsAllCreatedUsers() throws Exception {
        createUserViaApi("Ana Lopez", "ana@eci.edu.co", "11111111");
        createUserViaApi("Pedro Gomez", "pedro@eci.edu.co", "22222222");

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ── GET /api/users/{id} ───────────────────────────────────────────────────

    @Test
    void getById_returnsUser() throws Exception {
        long id = extractId(createUserViaApi("Luis Torres", "luis@eci.edu.co", "33333333"));

        mockMvc.perform(get("/api/users/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.fullName").value("Luis Torres"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getById_nonExistent_returns404() throws Exception {
        mockMvc.perform(get("/api/users/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").isString());
    }

    // ── GET /api/users/identification/{id} ───────────────────────────────────

    @Test
    void getByIdentification_returnsCorrectUser() throws Exception {
        createUserViaApi("Maria Ruiz", "maria@eci.edu.co", "55556666");

        mockMvc.perform(get("/api/users/identification/55556666"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identification").value("55556666"))
                .andExpect(jsonPath("$.fullName").value("Maria Ruiz"));
    }

    @Test
    void getByIdentification_nonExistent_returns404() throws Exception {
        mockMvc.perform(get("/api/users/identification/NOEXISTE"))
                .andExpect(status().isNotFound());
    }

    // ── PUT /api/users/{id} ───────────────────────────────────────────────────

    @Test
    void updateUser_changesDataInDatabase() throws Exception {
        long id = extractId(createUserViaApi("Nombre Viejo", "viejo@eci.edu.co", "77778888"));

        UserRequest updated = UserRequest.builder()
                .fullName("Nombre Nuevo")
                .email("nuevo@eci.edu.co")
                .password("hash")
                .identification("77778888")
                .build();

        mockMvc.perform(put("/api/users/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Nombre Nuevo"));

        mockMvc.perform(get("/api/users/" + id))
                .andExpect(jsonPath("$.fullName").value("Nombre Nuevo"));
    }

    @Test
    void updateUser_nonExistent_returns404() throws Exception {
        UserRequest request = UserRequest.builder()
                .fullName("X").email("x@x.com").password("x").identification("x")
                .build();

        mockMvc.perform(put("/api/users/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ── PATCH /api/users/{id}/deactivate ─────────────────────────────────────

    @Test
    void deactivateUser_setsStatusInactive() throws Exception {
        long id = extractId(createUserViaApi("Activo User", "activo@eci.edu.co", "99990000"));

        mockMvc.perform(patch("/api/users/" + id + "/deactivate"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/" + id))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    void deactivateUser_nonExistent_returns404() throws Exception {
        mockMvc.perform(patch("/api/users/9999/deactivate"))
                .andExpect(status().isNotFound());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /**
     * Creates a user through the public users API and returns the raw JSON
     * response body for follow-up assertions or id extraction.
     *
     * @param fullName full name to store for the user
     * @param email email address to store for the user
     * @param identification unique identification number to store for the user
     * @return JSON response returned by the create user endpoint
     * @throws Exception if the mock request fails
     */
    private String createUserViaApi(String fullName, String email, String identification)
            throws Exception {
        UserRequest request = UserRequest.builder()
                .fullName(fullName).email(email)
                .password("pass").identification(identification)
                .build();
        return mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();
    }

    /**
     * Extracts the numeric {@code id} field from a JSON response.
     *
     * @param json response body containing an {@code id} field
     * @return id value parsed as a long
     * @throws Exception if the response body cannot be parsed as JSON
     */
    private long extractId(String json) throws Exception {
        return objectMapper.readTree(json).get("id").asLong();
    }
}
