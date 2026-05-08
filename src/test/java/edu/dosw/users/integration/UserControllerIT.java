package edu.dosw.users.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.dosw.users.client.IdentityServiceClient;
import edu.dosw.users.dto.UserRequest;
import edu.dosw.users.model.UserModel;
import edu.dosw.users.repository.AuditLogRepository;
import edu.dosw.users.repository.InvitationRepository;
import edu.dosw.users.repository.SportProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@code UserController}.
 *
 * <p>The identity service is replaced with a {@link MockitoBean} so that the
 * Controller → Service → Client chain is exercised end-to-end without requiring
 * a running identity service.</p>
 */
@SpringBootTest
class UserControllerIT {

    @Autowired private WebApplicationContext context;
    @Autowired private SportProfileRepository sportProfileRepository;
    @Autowired private InvitationRepository invitationRepository;
    @Autowired private AuditLogRepository auditLogRepository;
    @MockitoBean private IdentityServiceClient identityServiceClient;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        auditLogRepository.deleteAll();
        invitationRepository.deleteAll();
        sportProfileRepository.deleteAll();
    }

    // ── POST /api/users ───────────────────────────────────────────────────────

    @Test
    void createUser_returns201() throws Exception {
        UserRequest request = UserRequest.builder()
                .fullName("Carlos Perez")
                .email("carlos@eci.edu.co")
                .password("hash123")
                .identification("11223344")
                .build();
        when(identityServiceClient.createUser(any())).thenReturn(
                UserModel.builder().id(1L).fullName("Carlos Perez").status("ACTIVE").build());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Carlos Perez"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    // ── GET /api/users ────────────────────────────────────────────────────────

    @Test
    void getAll_returnsUserList() throws Exception {
        when(identityServiceClient.getAllUsers()).thenReturn(List.of(
                UserModel.builder().id(1L).fullName("Ana Lopez").build(),
                UserModel.builder().id(2L).fullName("Pedro Gomez").build()));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ── GET /api/users/{id} ───────────────────────────────────────────────────

    @Test
    void getById_returnsUser() throws Exception {
        when(identityServiceClient.getUserById(1L)).thenReturn(
                UserModel.builder().id(1L).fullName("Luis Torres").build());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Luis Torres"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getById_nonExistent_returns404() throws Exception {
        when(identityServiceClient.getUserById(9999L)).thenReturn(null);

        mockMvc.perform(get("/api/users/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").isString());
    }

    // ── GET /api/users/identification/{id} ───────────────────────────────────

    @Test
    void getByIdentification_returnsCorrectUser() throws Exception {
        when(identityServiceClient.getUserByIdentification("55556666")).thenReturn(
                UserModel.builder().id(1L).identification("55556666").fullName("Maria Ruiz").build());

        mockMvc.perform(get("/api/users/identification/55556666"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identification").value("55556666"))
                .andExpect(jsonPath("$.fullName").value("Maria Ruiz"));
    }

    @Test
    void getByIdentification_nonExistent_returns404() throws Exception {
        when(identityServiceClient.getUserByIdentification("NOEXISTE")).thenReturn(null);

        mockMvc.perform(get("/api/users/identification/NOEXISTE"))
                .andExpect(status().isNotFound());
    }

    // ── PUT /api/users/{id} ───────────────────────────────────────────────────

    @Test
    void updateUser_returnsUpdatedData() throws Exception {
        when(identityServiceClient.getUserById(1L)).thenReturn(
                UserModel.builder().id(1L).fullName("Nombre Viejo").status("ACTIVE").build());
        when(identityServiceClient.updateUser(eq(1L), any())).thenReturn(
                UserModel.builder().id(1L).fullName("Nombre Nuevo").build());

        UserRequest updated = UserRequest.builder()
                .fullName("Nombre Nuevo")
                .email("nuevo@eci.edu.co")
                .password("hash")
                .identification("77778888")
                .build();

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Nombre Nuevo"));
    }

    @Test
    void updateUser_nonExistent_returns404() throws Exception {
        when(identityServiceClient.getUserById(9999L)).thenReturn(null);
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
    void deactivateUser_returnsNoContent() throws Exception {
        when(identityServiceClient.getUserById(1L)).thenReturn(
                UserModel.builder().id(1L).status("ACTIVE").build());
        doNothing().when(identityServiceClient).deactivateUser(1L);

        mockMvc.perform(patch("/api/users/1/deactivate"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deactivateUser_nonExistent_returns404() throws Exception {
        when(identityServiceClient.getUserById(9999L)).thenReturn(null);

        mockMvc.perform(patch("/api/users/9999/deactivate"))
                .andExpect(status().isNotFound());
    }
}
