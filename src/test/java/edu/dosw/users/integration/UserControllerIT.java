package edu.dosw.users.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.dosw.users.dto.AdminUserUpdateRequest;
import edu.dosw.users.entity.UserEntity;
import edu.dosw.users.repository.AuditLogRepository;
import edu.dosw.users.repository.InvitationRepository;
import edu.dosw.users.repository.SportProfileRepository;
import edu.dosw.users.repository.UserRepository;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for {@code UserController}.
 *
 * <p>The user repository is replaced with a {@link MockitoBean} so that the
 * Controller → Service → Repository chain is exercised end-to-end without
 * requiring a real database.</p>
 */
@SpringBootTest
@WithMockUser(roles = "ADMIN")
class UserControllerIT {

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
    }

    // ── GET /api/users ────────────────────────────────────────────────────────

    @Test
    void getAll_returnsUserList() throws Exception {
        UserEntity e1 = UserEntity.builder().id(1L).fullName("Ana Lopez").status("ACTIVE").build();
        UserEntity e2 = UserEntity.builder().id(2L).fullName("Pedro Gomez").status("ACTIVE").build();
        when(userRepository.findAll()).thenReturn(List.of(e1, e2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ── GET /api/users/{id} ───────────────────────────────────────────────────

    @Test
    void getById_returnsUser() throws Exception {
        UserEntity entity = UserEntity.builder().id(1L).fullName("Luis Torres").status("ACTIVE").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Luis Torres"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getById_nonExistent_returns404() throws Exception {
        when(userRepository.findById(9999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").isString());
    }

    // ── GET /api/users/identification/{id} ───────────────────────────────────

    @Test
    void getByIdentification_returnsCorrectUser() throws Exception {
        UserEntity entity = UserEntity.builder().id(1L).identification("55556666")
                .fullName("Maria Ruiz").status("ACTIVE").build();
        when(userRepository.findByIdentification("55556666")).thenReturn(Optional.of(entity));

        mockMvc.perform(get("/api/users/identification/55556666"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identification").value("55556666"))
                .andExpect(jsonPath("$.fullName").value("Maria Ruiz"));
    }

    @Test
    void getByIdentification_nonExistent_returns404() throws Exception {
        when(userRepository.findByIdentification("NOEXISTE")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/identification/NOEXISTE"))
                .andExpect(status().isNotFound());
    }

    // ── PUT /api/users/{id} ───────────────────────────────────────────────────

    @Test
    void updateUser_returnsUpdatedData() throws Exception {
        UserEntity entity = UserEntity.builder().id(1L).fullName("Nombre Viejo")
                .status("ACTIVE").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(userRepository.save(any())).thenReturn(
                UserEntity.builder().id(1L).fullName("Nombre Nuevo").status("ACTIVE").build());

        AdminUserUpdateRequest updated = AdminUserUpdateRequest.builder()
                .fullName("Nombre Nuevo")
                .build();

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Nombre Nuevo"));
    }

    @Test
    void updateUser_nonExistent_returns404() throws Exception {
        when(userRepository.findById(9999L)).thenReturn(Optional.empty());
        AdminUserUpdateRequest request = AdminUserUpdateRequest.builder()
                .fullName("X")
                .build();

        mockMvc.perform(put("/api/users/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ── PATCH /api/users/{id}/deactivate ─────────────────────────────────────

    @Test
    void deactivateUser_returnsNoContent() throws Exception {
        UserEntity entity = UserEntity.builder().id(1L).status("ACTIVE").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(userRepository.save(any())).thenReturn(entity);

        mockMvc.perform(patch("/api/users/1/deactivate"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deactivateUser_nonExistent_returns404() throws Exception {
        when(userRepository.findById(9999L)).thenReturn(Optional.empty());

        mockMvc.perform(patch("/api/users/9999/deactivate"))
                .andExpect(status().isNotFound());
    }
}
