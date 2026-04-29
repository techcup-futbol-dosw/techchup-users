package edu.dosw.users.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.dosw.users.dto.SportProfileRequest;
import edu.dosw.users.dto.UserRequest;
import edu.dosw.users.enums.Position;
import edu.dosw.users.repository.AuditLogRepository;
import edu.dosw.users.repository.InvitationRepository;
import edu.dosw.users.repository.SportProfileRepository;
import edu.dosw.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@code SportProfileController}.
 *
 * <p>Uses the full Spring context with a real H2 database and no mocked beans,
 * verifying that the Controller → Service → Repository chain works end-to-end.</p>
 */
@SpringBootTest
class SportProfileControllerIT {

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

    // ── POST /api/sport-profiles/user/{userId} ────────────────────────────────

    @Test
    void createSportProfile_persistsAndReturns201() throws Exception {
        long userId = createUser("Jugador Uno", "j1@eci.edu.co", "10001001");

        MockMultipartFile profilePart = buildProfilePart(Position.GOALKEEPER, 1, true);

        mockMvc.perform(multipart("/api/sport-profiles/user/" + userId)
                        .file(profilePart))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.position").value("GOALKEEPER"))
                .andExpect(jsonPath("$.dorsalNumber").value(1))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void createSportProfile_nonExistentUser_returns404() throws Exception {
        MockMultipartFile profilePart = buildProfilePart(Position.DEFENDER, 5, false);

        mockMvc.perform(multipart("/api/sport-profiles/user/9999")
                        .file(profilePart))
                .andExpect(status().isNotFound());
    }

    @Test
    void createSportProfile_duplicateForSameUser_returns409() throws Exception {
        long userId = createUser("Jugador Dos", "j2@eci.edu.co", "10002002");
        MockMultipartFile profilePart = buildProfilePart(Position.MIDFIELDER, 8, true);

        mockMvc.perform(multipart("/api/sport-profiles/user/" + userId).file(profilePart));

        mockMvc.perform(multipart("/api/sport-profiles/user/" + userId)
                        .file(profilePart))
                .andExpect(status().isConflict());
    }

    // ── GET /api/sport-profiles/{id} ─────────────────────────────────────────

    @Test
    void getById_returnsSportProfile() throws Exception {
        long userId = createUser("Jugador Tres", "j3@eci.edu.co", "10003003");
        long profileId = createSportProfile(userId, Position.FORWARD, 9, false);

        mockMvc.perform(get("/api/sport-profiles/" + profileId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(profileId))
                .andExpect(jsonPath("$.position").value("FORWARD"));
    }

    @Test
    void getById_nonExistent_returns404() throws Exception {
        mockMvc.perform(get("/api/sport-profiles/9999"))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/sport-profiles/user/{userId} ─────────────────────────────────

    @Test
    void getByUserId_returnsSportProfile() throws Exception {
        long userId = createUser("Jugador Cuatro", "j4@eci.edu.co", "10004004");
        createSportProfile(userId, Position.DEFENDER, 4, true);

        mockMvc.perform(get("/api/sport-profiles/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId))
                .andExpect(jsonPath("$.position").value("DEFENDER"));
    }

    @Test
    void getByUserId_noProfile_returns404() throws Exception {
        long userId = createUser("Jugador Cinco", "j5@eci.edu.co", "10005005");

        mockMvc.perform(get("/api/sport-profiles/user/" + userId))
                .andExpect(status().isNotFound());
    }

    // ── PUT /api/sport-profiles/{id} ──────────────────────────────────────────

    @Test
    void updateSportProfile_changesData() throws Exception {
        long userId = createUser("Jugador Seis", "j6@eci.edu.co", "10006006");
        long profileId = createSportProfile(userId, Position.GOALKEEPER, 1, true);

        MockMultipartFile updatedPart = buildProfilePart(Position.FORWARD, 11, false);

        mockMvc.perform(multipart("/api/sport-profiles/" + profileId)
                        .file(updatedPart)
                        .with(req -> { req.setMethod("PUT"); return req; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value("FORWARD"))
                .andExpect(jsonPath("$.dorsalNumber").value(11))
                .andExpect(jsonPath("$.available").value(false));
    }

    // ── PATCH /api/sport-profiles/{id}/availability ───────────────────────────

    @Test
    void updateAvailability_changesFlag() throws Exception {
        long userId = createUser("Jugador Siete", "j7@eci.edu.co", "10007007");
        long profileId = createSportProfile(userId, Position.MIDFIELDER, 6, true);

        mockMvc.perform(patch("/api/sport-profiles/" + profileId + "/availability")
                        .param("available", "false"))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/sport-profiles/" + profileId))
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void updateAvailability_nonExistent_returns404() throws Exception {
        mockMvc.perform(patch("/api/sport-profiles/9999/availability")
                        .param("available", "true"))
                .andExpect(status().isNotFound());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /**
     * Creates a user through the public users API and returns the generated id.
     *
     * @param fullName full name to store for the user
     * @param email email address to store for the user
     * @param identification unique identification number to store for the user
     * @return generated user identifier extracted from the JSON response
     * @throws Exception if the mock request or JSON parsing fails
     */
    private long createUser(String fullName, String email, String identification) throws Exception {
        UserRequest request = UserRequest.builder()
                .fullName(fullName).email(email)
                .password("pass").identification(identification)
                .build();
        String response = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    /**
     * Creates a sport profile through the public sport profile API and returns
     * the generated profile id.
     *
     * @param userId identifier of the user who owns the sport profile
     * @param position player position to include in the multipart profile part
     * @param dorsal jersey number to include in the multipart profile part
     * @param available availability flag to include in the multipart profile part
     * @return generated sport profile identifier extracted from the JSON response
     * @throws Exception if the mock request or JSON parsing fails
     */
    private long createSportProfile(Long userId, Position position, int dorsal, boolean available)
            throws Exception {
        MockMultipartFile profilePart = buildProfilePart(position, dorsal, available);
        String response = mockMvc.perform(multipart("/api/sport-profiles/user/" + userId)
                        .file(profilePart))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    /**
     * Builds the JSON multipart part used by create and update sport profile
     * requests.
     *
     * @param position player position to serialize
     * @param dorsal jersey number to serialize
     * @param available availability flag to serialize
     * @return multipart file named {@code profile} with JSON content
     * @throws Exception if JSON serialization fails
     */
    private MockMultipartFile buildProfilePart(Position position, int dorsal, boolean available)
            throws Exception {
        SportProfileRequest request = SportProfileRequest.builder()
                .position(position).dorsalNumber(dorsal).available(available)
                .build();
        return new MockMultipartFile(
                "profile", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));
    }
}
