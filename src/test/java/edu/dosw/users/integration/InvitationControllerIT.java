package edu.dosw.users.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.dosw.users.repository.UserRepository;
import edu.dosw.users.repository.AuditLogRepository;
import edu.dosw.users.repository.InvitationRepository;
import edu.dosw.users.repository.SportProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@code InvitationController}.
 *
 * <p>Uses the full Spring context with a real H2 database. The identity
 * service is replaced with a {@link MockitoBean} that confirms player existence
 * for valid IDs and denies it for unknown ones.</p>
 */
@SpringBootTest
@WithMockUser(roles = {"ADMIN", "CAPTAIN"})
class InvitationControllerIT {

    @Autowired private WebApplicationContext context;
    @Autowired private SportProfileRepository sportProfileRepository;
    @Autowired private InvitationRepository invitationRepository;
    @Autowired private AuditLogRepository auditLogRepository;
    @MockitoBean private UserRepository userRepository;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        auditLogRepository.deleteAll();
        invitationRepository.deleteAll();
        sportProfileRepository.deleteAll();
        // All player IDs are considered valid by default
        when(userRepository.existsById(any(Long.class))).thenReturn(true);
    }

    // ── POST /api/invitations/user/{playerId}/team/{teamId} ─────────────────

    @Test
    void sendInvitation_persistsAndReturnsPending() throws Exception {
        mockMvc.perform(post("/api/invitations/user/1/team/100"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.playerId").value(1))
                .andExpect(jsonPath("$.teamId").value(100))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void sendDuplicateInvitation_returns409() throws Exception {
        mockMvc.perform(post("/api/invitations/user/2/team/200"));

        mockMvc.perform(post("/api/invitations/user/2/team/200"))
                .andExpect(status().isConflict());
    }

    // ── GET /api/invitations/{id} ─────────────────────────────────────────────

    @Test
    void getById_returnsInvitation() throws Exception {
        long invitationId = sendInvitation(3L, 300L);

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

    // ── GET /api/invitations/user/{playerId} ────────────────────────────────

    @Test
    void getByPlayerId_returnsInvitationList() throws Exception {
        sendInvitation(4L, 401L);
        sendInvitation(4L, 402L);

        mockMvc.perform(get("/api/invitations/user/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ── PATCH /api/invitations/{id}/accept ────────────────────────────────────

    @Test
    void acceptInvitation_changesStatusToAccepted() throws Exception {
        long invitationId = sendInvitation(5L, 500L);

        mockMvc.perform(patch("/api/invitations/" + invitationId + "/accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.respondedAt").isNotEmpty());
    }

    @Test
    void acceptInvitation_alreadyAccepted_returns409() throws Exception {
        long invitationId = sendInvitation(6L, 600L);

        mockMvc.perform(patch("/api/invitations/" + invitationId + "/accept"));

        mockMvc.perform(patch("/api/invitations/" + invitationId + "/accept"))
                .andExpect(status().isConflict());
    }

    // ── PATCH /api/invitations/{id}/reject ────────────────────────────────────

    @Test
    void rejectInvitation_changesStatusToRejected() throws Exception {
        long invitationId = sendInvitation(7L, 700L);

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
        long invitationId = sendInvitation(8L, 800L);

        mockMvc.perform(patch("/api/invitations/" + invitationId + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancelInvitation_alreadyRejected_returns409() throws Exception {
        long invitationId = sendInvitation(9L, 900L);

        mockMvc.perform(patch("/api/invitations/" + invitationId + "/reject"));

        mockMvc.perform(patch("/api/invitations/" + invitationId + "/cancel"))
                .andExpect(status().isConflict());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private long sendInvitation(long playerId, long teamId) throws Exception {
        String response = mockMvc.perform(
                        post("/api/invitations/user/" + playerId + "/team/" + teamId))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }
}
