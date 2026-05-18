package edu.dosw.users.controller;

import edu.dosw.users.enums.AuditAction;
import edu.dosw.users.model.AuditLogModel;
import edu.dosw.users.service.IAuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web layer tests for audit log endpoints.
 *
 * <p>Uses {@link MockMvc} with a mocked {@link IAuditService} to verify
 * HTTP status codes and JSON responses for audit log queries.</p>
 */
@SpringBootTest
@WithMockUser(roles = "ADMINISTRADOR")
class AuditLogControllerTest {

    @Autowired private WebApplicationContext context;
    @MockitoBean private IAuditService auditService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // ── GET /api/audit-logs/sport-profiles/{sportProfileId} ──────────────────

    @Test
    void getBySportProfile_returnsOkWithList() throws Exception {
        when(auditService.getLogsForSportProfile(1L)).thenReturn(List.of(
                AuditLogModel.builder().id(1L).sportProfileId(1L)
                        .action(AuditAction.CREATE).timestamp(LocalDateTime.now())
                        .details("Profile created").build(),
                AuditLogModel.builder().id(2L).sportProfileId(1L)
                        .action(AuditAction.UPDATE).timestamp(LocalDateTime.now())
                        .details("Position changed").build()));

        mockMvc.perform(get("/api/audit-logs/sport-profiles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].action").value("CREATE"))
                .andExpect(jsonPath("$[1].action").value("UPDATE"));
    }

    @Test
    void getBySportProfile_noLogs_returnsEmptyList() throws Exception {
        when(auditService.getLogsForSportProfile(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/audit-logs/sport-profiles/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── GET /api/audit-logs/invitations/{invitationId} ───────────────────────

    @Test
    void getByInvitation_returnsOkWithList() throws Exception {
        when(auditService.getLogsForInvitation(5L)).thenReturn(List.of(
                AuditLogModel.builder().id(3L).invitationId(5L)
                        .action(AuditAction.CREATE).timestamp(LocalDateTime.now())
                        .details("Invitation sent").build()));

        mockMvc.perform(get("/api/audit-logs/invitations/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].invitationId").value(5))
                .andExpect(jsonPath("$[0].action").value("CREATE"));
    }

    @Test
    void getByInvitation_noLogs_returnsEmptyList() throws Exception {
        when(auditService.getLogsForInvitation(99L)).thenReturn(List.of());

        mockMvc.perform(get("/api/audit-logs/invitations/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}