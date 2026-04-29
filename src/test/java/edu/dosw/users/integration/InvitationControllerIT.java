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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@code InvitationController}.
 *
 * <p>Uses the full Spring context with a real H2 database and no mocked beans,
 * verifying that the Controller → Service → Repository chain works end-to-end.</p>
 */
@SpringBootTest
class InvitationControllerIT {

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

    // ── POST /api/invitations/player/{playerId}/team/{teamId} ─────────────────

    @Test
    void sendInvitation_persistsAndReturnsPending() throws Exception {
        long playerId = createUser("Jugador A", "ja@eci.edu.co", "20001001");

        mockMvc.perform(post("/api/invitations/player/" + playerId + "/team/100"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.playerId").value(playerId))
                .andExpect(jsonPath("$.teamId").value(100))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void sendInvitation_playerNotFound_returns404() throws Exception {
        mockMvc.perform(post("/api/invitations/player/9999/team/100"))
                .andExpect(status().isNotFound());
    }

    @Test
    void sendDuplicateInvitation_returns409() throws Exception {
        long playerId = createUser("Jugador B", "jb@eci.edu.co", "20002002");

        mockMvc.perform(post("/api/invitations/player/" + playerId + "/team/200"));

        mockMvc.perform(post("/api/invitations/player/" + playerId + "/team/200"))
                .andExpect(status().isConflict());
    }

    // ── GET /api/invitations/{id} ─────────────────────────────────────────────

    @Test
    void getById_returnsInvitation() throws Exception {
        long playerId = createUser("Jugador C", "jc@eci.edu.co", "20003003");
        long invitationId = sendInvitation(playerId, 300L);

        mockMvc.perform(get("/api/invitations/" + invitationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(invitationId))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getById_nonExistent_returns404() throws Exception {
        mockMvc.perform(get("/api/invitations/9999"))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/invitations/player/{playerId} ────────────────────────────────

    @Test
    void getByPlayerId_returnsInvitationList() throws Exception {
        long playerId = createUser("Jugador D", "jd@eci.edu.co", "20004004");
        sendInvitation(playerId, 401L);
        sendInvitation(playerId, 402L);

        mockMvc.perform(get("/api/invitations/player/" + playerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ── PATCH /api/invitations/{id}/accept ────────────────────────────────────

    @Test
    void acceptInvitation_changesStatusToAccepted() throws Exception {
        long playerId = createUser("Jugador E", "je@eci.edu.co", "20005005");
        long invitationId = sendInvitation(playerId, 500L);

        mockMvc.perform(patch("/api/invitations/" + invitationId + "/accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.respondedAt").isNotEmpty());
    }

    @Test
    void acceptInvitation_alreadyAccepted_returns409() throws Exception {
        long playerId = createUser("Jugador F", "jf@eci.edu.co", "20006006");
        long invitationId = sendInvitation(playerId, 600L);

        mockMvc.perform(patch("/api/invitations/" + invitationId + "/accept"));

        mockMvc.perform(patch("/api/invitations/" + invitationId + "/accept"))
                .andExpect(status().isConflict());
    }

    // ── PATCH /api/invitations/{id}/reject ────────────────────────────────────

    @Test
    void rejectInvitation_changesStatusToRejected() throws Exception {
        long playerId = createUser("Jugador G", "jg@eci.edu.co", "20007007");
        long invitationId = sendInvitation(playerId, 700L);

        mockMvc.perform(patch("/api/invitations/" + invitationId + "/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void rejectInvitation_nonExistent_returns404() throws Exception {
        mockMvc.perform(patch("/api/invitations/9999/reject"))
                .andExpect(status().isNotFound());
    }

    // ── PATCH /api/invitations/{id}/cancel ────────────────────────────────────

    @Test
    void cancelInvitation_changesStatusToCancelled() throws Exception {
        long playerId = createUser("Jugador H", "jh@eci.edu.co", "20008008");
        long invitationId = sendInvitation(playerId, 800L);

        mockMvc.perform(patch("/api/invitations/" + invitationId + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancelInvitation_alreadyRejected_returns409() throws Exception {
        long playerId = createUser("Jugador I", "ji@eci.edu.co", "20009009");
        long invitationId = sendInvitation(playerId, 900L);

        mockMvc.perform(patch("/api/invitations/" + invitationId + "/reject"));

        mockMvc.perform(patch("/api/invitations/" + invitationId + "/cancel"))
                .andExpect(status().isConflict());
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
     * Sends an invitation through the public invitations API and returns the
     * generated invitation id.
     *
     * @param playerId identifier of the player receiving the invitation
     * @param teamId identifier of the team extending the invitation
     * @return generated invitation identifier extracted from the JSON response
     * @throws Exception if the mock request or JSON parsing fails
     */
    private long sendInvitation(long playerId, long teamId) throws Exception {
        String response = mockMvc.perform(
                        post("/api/invitations/player/" + playerId + "/team/" + teamId))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }
}
