package edu.dosw.users.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.dosw.users.repository.UserRepository;
import edu.dosw.users.dto.SportProfileRequest;
import edu.dosw.users.enums.Position;
import edu.dosw.users.repository.AuditLogRepository;
import edu.dosw.users.repository.InvitationRepository;
import edu.dosw.users.repository.SportProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@code SportProfileController}.
 *
 * <p>Uses the full Spring context with a real H2 database. The identity
 * service is replaced with a {@link MockitoBean} that confirms user existence
 * for valid IDs and denies it for unknown ones.</p>
 */
@SpringBootTest
@WithMockUser(roles = "ADMIN")
class SportProfileControllerIT {

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
        // All user IDs are considered valid by default
        when(userRepository.existsById(any(Long.class))).thenReturn(true);
    }

    // ── POST /api/sport-profiles/user/{userId} ────────────────────────────────

    @Test
    void createSportProfile_persistsAndReturns201() throws Exception {
        mockMvc.perform(post("/api/sport-profiles/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildProfileJson(Position.GOALKEEPER, 1, true)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.position").value("GOALKEEPER"))
                .andExpect(jsonPath("$.dorsalNumber").value(1))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    void createSportProfile_duplicateForSameUser_returns409() throws Exception {
        mockMvc.perform(post("/api/sport-profiles/user/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(buildProfileJson(Position.MIDFIELDER, 8, true)));

        mockMvc.perform(post("/api/sport-profiles/user/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildProfileJson(Position.MIDFIELDER, 8, true)))
                .andExpect(status().isConflict());
    }

    // ── GET /api/sport-profiles/{id} ─────────────────────────────────────────

    @Test
    void getById_returnsSportProfile() throws Exception {
        long profileId = createSportProfile(3L, Position.FORWARD, 9, false);

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
        createSportProfile(4L, Position.DEFENDER, 4, true);

        mockMvc.perform(get("/api/sport-profiles/user/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(4))
                .andExpect(jsonPath("$.position").value("DEFENDER"));
    }

    @Test
    void getByUserId_noProfile_returns404() throws Exception {
        mockMvc.perform(get("/api/sport-profiles/user/5"))
                .andExpect(status().isNotFound());
    }

    // ── PUT /api/sport-profiles/{id} ──────────────────────────────────────────

    @Test
    void updateSportProfile_changesData() throws Exception {
        long profileId = createSportProfile(6L, Position.GOALKEEPER, 1, true);

        mockMvc.perform(put("/api/sport-profiles/" + profileId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildProfileJson(Position.FORWARD, 11, false)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value("FORWARD"))
                .andExpect(jsonPath("$.dorsalNumber").value(11))
                .andExpect(jsonPath("$.available").value(false));
    }

    // ── PATCH /api/sport-profiles/{id}/availability ───────────────────────────

    @Test
    void updateAvailability_changesFlag() throws Exception {
        long profileId = createSportProfile(7L, Position.MIDFIELDER, 6, true);

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

    private long createSportProfile(Long userId, Position position, int dorsal, boolean available)
            throws Exception {
        String response = mockMvc.perform(post("/api/sport-profiles/user/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(buildProfileJson(position, dorsal, available)))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asLong();
    }

    private String buildProfileJson(Position position, int dorsal, boolean available)
            throws Exception {
        return objectMapper.writeValueAsString(
                SportProfileRequest.builder()
                        .position(position).dorsalNumber(dorsal).available(available)
                        .build());
    }
}
