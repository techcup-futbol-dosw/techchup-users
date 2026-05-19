package edu.dosw.users.controller;

import edu.dosw.users.exception.BusinessException;
import edu.dosw.users.exception.ResourceNotFoundException;
import edu.dosw.users.enums.InvitationStatus;
import edu.dosw.users.model.InvitationModel;
import edu.dosw.users.service.IInvitationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web layer tests for invitation endpoints.
 *
 * <p>Uses {@link MockMvc} with a mocked {@link IInvitationService} to verify
 * HTTP status codes and JSON responses produced by the invitation controller,
 * including successful operations and expected error mappings.</p>
 */
@SpringBootTest
@WithMockUser(roles = {"ADMIN", "CAPITAN"})
class InvitationControllerTest {

    @Autowired private WebApplicationContext context;
    @MockitoBean private IInvitationService invitationService;

    private MockMvc mockMvc;

    /**
     * Builds the {@link MockMvc} instance from the web application context
     * before each test case.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // ── GET /api/invitations/{id} ─────────────────────────────────────────────

    @Test
    void getById_found_returnsOk() throws Exception {
        when(invitationService.getById(1L))
                .thenReturn(InvitationModel.builder().id(1L).teamId(5L)
                        .status(InvitationStatus.PENDING).build());

        mockMvc.perform(get("/api/invitations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(invitationService.getById(99L))
                .thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(get("/api/invitations/99"))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/invitations/user/{userId} ───────────────────────────────────

    @Test
    void getByUserId_returnsOkWithList() throws Exception {
        when(invitationService.getByPlayerId(10L)).thenReturn(List.of(
                InvitationModel.builder().id(1L).playerId(10L).build(),
                InvitationModel.builder().id(2L).playerId(10L).build()));

        mockMvc.perform(get("/api/invitations/user/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ── POST /api/invitations/user/{userId}/team/{teamId} ────────────────────

    @Test
    void send_returnsCreated() throws Exception {
        when(invitationService.send(10L, 5L))
                .thenReturn(InvitationModel.builder().id(3L).playerId(10L).teamId(5L)
                        .status(InvitationStatus.PENDING).build());

        mockMvc.perform(post("/api/invitations/user/10/team/5"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void send_userNotFound_returns404() throws Exception {
        when(invitationService.send(99L, 5L))
                .thenThrow(new ResourceNotFoundException("user not found"));

        mockMvc.perform(post("/api/invitations/user/99/team/5"))
                .andExpect(status().isNotFound());
    }

    @Test
    void send_pendingAlreadyExists_returns409() throws Exception {
        when(invitationService.send(10L, 5L))
                .thenThrow(new BusinessException("already pending"));

        mockMvc.perform(post("/api/invitations/user/10/team/5"))
                .andExpect(status().isConflict());
    }

    // ── PATCH /api/invitations/{id}/accept ────────────────────────────────────

    @Test
    void accept_returnsOk() throws Exception {
        when(invitationService.accept(1L))
                .thenReturn(InvitationModel.builder().id(1L)
                        .status(InvitationStatus.ACCEPTED).build());

        mockMvc.perform(patch("/api/invitations/1/accept"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void accept_nonPending_returns409() throws Exception {
        when(invitationService.accept(1L))
                .thenThrow(new BusinessException("not pending"));

        mockMvc.perform(patch("/api/invitations/1/accept"))
                .andExpect(status().isConflict());
    }

    // ── PATCH /api/invitations/{id}/reject ────────────────────────────────────

    @Test
    void reject_returnsOk() throws Exception {
        when(invitationService.reject(2L))
                .thenReturn(InvitationModel.builder().id(2L)
                        .status(InvitationStatus.REJECTED).build());

        mockMvc.perform(patch("/api/invitations/2/reject"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    @Test
    void reject_notFound_returns404() throws Exception {
        when(invitationService.reject(99L))
                .thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(patch("/api/invitations/99/reject"))
                .andExpect(status().isNotFound());
    }

    // ── PATCH /api/invitations/{id}/cancel ────────────────────────────────────

    @Test
    void cancel_returnsOk() throws Exception {
        when(invitationService.cancel(3L))
                .thenReturn(InvitationModel.builder().id(3L)
                        .status(InvitationStatus.CANCELLED).build());

        mockMvc.perform(patch("/api/invitations/3/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void cancel_notFound_returns404() throws Exception {
        when(invitationService.cancel(99L))
                .thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(patch("/api/invitations/99/cancel"))
                .andExpect(status().isNotFound());
    }
}
