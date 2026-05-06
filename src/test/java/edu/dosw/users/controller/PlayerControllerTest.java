package edu.dosw.users.controller;

import edu.dosw.users.exception.ResourceNotFoundException;
import edu.dosw.users.model.SportProfileModel;
import edu.dosw.users.service.ISportProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web layer tests for player-centric endpoints.
 *
 * <p>Uses {@link MockMvc} with a mocked {@link ISportProfileService} to verify
 * successful responses and exception-to-status mappings for
 * {@code GET /api/players/{id}/profile}.</p>
 */
@SpringBootTest
class PlayerControllerTest {

    @Autowired private WebApplicationContext context;
    @MockitoBean private ISportProfileService sportProfileService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // ── GET /api/players/{id}/profile ────────────────────────────────────────

    @Test
    void getProfile_found_returnsOk() throws Exception {
        when(sportProfileService.getByUserId(8L))
                .thenReturn(SportProfileModel.builder().id(3L).userId(8L).build());

        mockMvc.perform(get("/api/players/8/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.userId").value(8));
    }

    @Test
    void getProfile_notFound_returns404() throws Exception {
        when(sportProfileService.getByUserId(99L))
                .thenThrow(new ResourceNotFoundException("Sport profile not found for user id: 99"));

        mockMvc.perform(get("/api/players/99/profile"))
                .andExpect(status().isNotFound());
    }
}