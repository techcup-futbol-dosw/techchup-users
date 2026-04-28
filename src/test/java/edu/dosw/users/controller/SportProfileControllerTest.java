package edu.dosw.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.dosw.users.exception.BusinessException;
import edu.dosw.users.exception.ResourceNotFoundException;
import edu.dosw.users.model.SportProfileModel;
import edu.dosw.users.service.ISportProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SportProfileControllerTest {

    @Autowired private WebApplicationContext context;
    @MockitoBean private ISportProfileService sportProfileService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // ── GET /api/sport-profiles/{id} ─────────────────────────────────────────

    @Test
    void getById_found_returnsOk() throws Exception {
        when(sportProfileService.getById(1L))
                .thenReturn(SportProfileModel.builder().id(1L).userId(10L).build());

        mockMvc.perform(get("/api/sport-profiles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(10));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(sportProfileService.getById(99L))
                .thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(get("/api/sport-profiles/99"))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/sport-profiles/user/{userId} ─────────────────────────────────

    @Test
    void getByUserId_found_returnsOk() throws Exception {
        when(sportProfileService.getByUserId(5L))
                .thenReturn(SportProfileModel.builder().id(2L).userId(5L).build());

        mockMvc.perform(get("/api/sport-profiles/user/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(5));
    }

    @Test
    void getByUserId_notFound_returns404() throws Exception {
        when(sportProfileService.getByUserId(99L))
                .thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(get("/api/sport-profiles/user/99"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/sport-profiles/user/{userId} ────────────────────────────────

    @Test
    void create_returnsCreated() throws Exception {
        SportProfileModel saved = SportProfileModel.builder().id(3L).userId(1L).build();
        when(sportProfileService.create(eq(1L), any(), any())).thenReturn(saved);

        MockMultipartFile profilePart = new MockMultipartFile(
                "profile", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(SportProfileModel.builder().build()));

        mockMvc.perform(multipart("/api/sport-profiles/user/1")
                        .file(profilePart))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    void create_userNotFound_returns404() throws Exception {
        when(sportProfileService.create(eq(99L), any(), any()))
                .thenThrow(new ResourceNotFoundException("user not found"));

        MockMultipartFile profilePart = new MockMultipartFile(
                "profile", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(SportProfileModel.builder().build()));

        mockMvc.perform(multipart("/api/sport-profiles/user/99")
                        .file(profilePart))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_alreadyHasProfile_returns409() throws Exception {
        when(sportProfileService.create(eq(1L), any(), any()))
                .thenThrow(new BusinessException("already has profile"));

        MockMultipartFile profilePart = new MockMultipartFile(
                "profile", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(SportProfileModel.builder().build()));

        mockMvc.perform(multipart("/api/sport-profiles/user/1")
                        .file(profilePart))
                .andExpect(status().isConflict());
    }

    // ── PUT /api/sport-profiles/{id} ──────────────────────────────────────────

    @Test
    void update_returnsOk() throws Exception {
        SportProfileModel updated = SportProfileModel.builder().id(1L).build();
        when(sportProfileService.update(eq(1L), any(), any())).thenReturn(updated);

        MockMultipartFile profilePart = new MockMultipartFile(
                "profile", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(SportProfileModel.builder().build()));

        mockMvc.perform(multipart("/api/sport-profiles/1")
                        .file(profilePart)
                        .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void update_playerInTeam_returns409() throws Exception {
        when(sportProfileService.update(eq(1L), any(), any()))
                .thenThrow(new BusinessException("player in team"));

        MockMultipartFile profilePart = new MockMultipartFile(
                "profile", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(SportProfileModel.builder().build()));

        mockMvc.perform(multipart("/api/sport-profiles/1")
                        .file(profilePart)
                        .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isConflict());
    }

    // ── PATCH /api/sport-profiles/{id}/availability ───────────────────────────

    @Test
    void updateAvailability_returnsNoContent() throws Exception {
        doNothing().when(sportProfileService).updateAvailability(1L, true);

        mockMvc.perform(patch("/api/sport-profiles/1/availability")
                        .param("available", "true"))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateAvailability_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("not found"))
                .when(sportProfileService).updateAvailability(99L, false);

        mockMvc.perform(patch("/api/sport-profiles/99/availability")
                        .param("available", "false"))
                .andExpect(status().isNotFound());
    }
}