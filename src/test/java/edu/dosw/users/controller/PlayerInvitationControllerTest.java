package edu.dosw.users.controller;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import edu.dosw.users.enums.InvitationStatus;
import edu.dosw.users.model.InvitationModel;
import edu.dosw.users.service.IInvitationService;

/**
 * Web layer tests for player invitation endpoints.
 */
@SpringBootTest
class PlayerInvitationControllerTest {

    @Autowired private WebApplicationContext context;
    @MockitoBean private IInvitationService invitationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void getMyInvitations_returnsOk() throws Exception {
        when(invitationService.getByPlayerId(7L, null)).thenReturn(List.of(
                InvitationModel.builder().id(1L).playerId(7L).teamId(100L)
                        .status(InvitationStatus.PENDING).build(),
                InvitationModel.builder().id(2L).playerId(7L).teamId(200L)
                        .status(InvitationStatus.ACCEPTED).build()));

        mockMvc.perform(get("/api/players/me/invitations")
                        .header("X-User-Id", 7L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getMyInvitations_withStatus_returnsOk() throws Exception {
        when(invitationService.getByPlayerId(7L, InvitationStatus.PENDING))
                .thenReturn(List.of(
                        InvitationModel.builder().id(3L).playerId(7L).teamId(300L)
                                .status(InvitationStatus.PENDING).build()));

        mockMvc.perform(get("/api/players/me/invitations")
                        .header("X-User-Id", 7L)
                        .param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }
}
