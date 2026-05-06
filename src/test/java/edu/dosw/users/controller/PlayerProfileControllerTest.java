package edu.dosw.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.dosw.users.dto.SportProfileRequest;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web layer tests for player sport profile endpoints.
 */
@SpringBootTest
class PlayerProfileControllerTest {

    @Autowired private WebApplicationContext context;
    @MockitoBean private ISportProfileService sportProfileService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void updateMyProfile_returnsOk() throws Exception {
        SportProfileModel updated = SportProfileModel.builder().id(10L).userId(7L).build();
        when(sportProfileService.updateByUserId(eq(7L), any(), any())).thenReturn(updated);

        MockMultipartFile profilePart = new MockMultipartFile(
                "profile", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(SportProfileRequest.builder().build()));

        mockMvc.perform(multipart("/api/players/me/profile")
                        .file(profilePart)
                        .header("X-User-Id", 7L)
                        .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.userId").value(7));
    }

    @Test
    void updateMyProfile_playerInTeam_returns409() throws Exception {
        when(sportProfileService.updateByUserId(eq(7L), any(), any()))
                .thenThrow(new BusinessException("player in team"));

        MockMultipartFile profilePart = new MockMultipartFile(
                "profile", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(SportProfileRequest.builder().build()));

        mockMvc.perform(multipart("/api/players/me/profile")
                        .file(profilePart)
                        .header("X-User-Id", 7L)
                        .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isConflict());
    }

    @Test
    void updateMyProfile_noProfile_returns404() throws Exception {
        when(sportProfileService.updateByUserId(eq(7L), any(), any()))
                .thenThrow(new ResourceNotFoundException("not found"));

        MockMultipartFile profilePart = new MockMultipartFile(
                "profile", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(SportProfileRequest.builder().build()));

        mockMvc.perform(multipart("/api/players/me/profile")
                        .file(profilePart)
                        .header("X-User-Id", 7L)
                        .with(request -> { request.setMethod("PUT"); return request; }))
                .andExpect(status().isNotFound());
    }
}
