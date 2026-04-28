package edu.dosw.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.dosw.users.exception.ResourceNotFoundException;
import edu.dosw.users.model.UserProfileModel;
import edu.dosw.users.service.IUserProfileService;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class UserProfileControllerTest {

    @Autowired private WebApplicationContext context;
    @MockitoBean private IUserProfileService userProfileService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // ── GET /api/users ────────────────────────────────────────────────────────

    @Test
    void getAll_returnsOkWithList() throws Exception {
        UserProfileModel m1 = UserProfileModel.builder().id(1L).fullName("Carlos").build();
        UserProfileModel m2 = UserProfileModel.builder().id(2L).fullName("Maria").build();
        when(userProfileService.getAll()).thenReturn(List.of(m1, m2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fullName").value("Carlos"));
    }

    // ── GET /api/users/{id} ──────────────────────────────────────────────────

    @Test
    void getById_found_returnsOk() throws Exception {
        when(userProfileService.getById(1L))
                .thenReturn(UserProfileModel.builder().id(1L).fullName("Carlos").build());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Carlos"));
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(userProfileService.getById(99L))
                .thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not found"));
    }

    // ── GET /api/users/identification/{id} ───────────────────────────────────

    @Test
    void getByIdentification_found_returnsOk() throws Exception {
        when(userProfileService.getByIdentification("12345"))
                .thenReturn(UserProfileModel.builder().id(1L).identification("12345").build());

        mockMvc.perform(get("/api/users/identification/12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identification").value("12345"));
    }

    @Test
    void getByIdentification_notFound_returns404() throws Exception {
        when(userProfileService.getByIdentification("xxx"))
                .thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(get("/api/users/identification/xxx"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/users ───────────────────────────────────────────────────────

    @Test
    void create_returnsCreated() throws Exception {
        UserProfileModel input = UserProfileModel.builder()
                .fullName("Ana").email("ana@eci.edu.co")
                .password("hash").identification("999")
                .build();
        UserProfileModel saved = UserProfileModel.builder().id(5L).fullName("Ana").build();
        when(userProfileService.create(any())).thenReturn(saved);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    // ── PUT /api/users/{id} ───────────────────────────────────────────────────

    @Test
    void update_returnsOk() throws Exception {
        UserProfileModel input = UserProfileModel.builder().fullName("Nuevo nombre").build();
        UserProfileModel updated = UserProfileModel.builder().id(1L).fullName("Nuevo nombre").build();
        when(userProfileService.update(eq(1L), any())).thenReturn(updated);

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Nuevo nombre"));
    }

    @Test
    void update_notFound_returns404() throws Exception {
        when(userProfileService.update(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(put("/api/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                UserProfileModel.builder().fullName("x").build())))
                .andExpect(status().isNotFound());
    }

    // ── PATCH /api/users/{id}/deactivate ─────────────────────────────────────

    @Test
    void deactivate_returnsNoContent() throws Exception {
        doNothing().when(userProfileService).deactivate(1L);

        mockMvc.perform(patch("/api/users/1/deactivate"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deactivate_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("not found"))
                .when(userProfileService).deactivate(99L);

        mockMvc.perform(patch("/api/users/99/deactivate"))
                .andExpect(status().isNotFound());
    }
}