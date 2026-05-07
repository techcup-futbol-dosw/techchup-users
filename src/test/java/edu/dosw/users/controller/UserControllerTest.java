package edu.dosw.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.dosw.users.dto.UserProfileUpdateRequest;
import edu.dosw.users.dto.UserRequest;
import edu.dosw.users.exception.ResourceNotFoundException;
import edu.dosw.users.enums.Gender;
import edu.dosw.users.enums.SchoolRelation;
import edu.dosw.users.mapper.UserMapper;
import edu.dosw.users.exception.BusinessException;
import edu.dosw.users.model.UserModel;
import edu.dosw.users.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
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

/**
 * Web layer tests for user profile endpoints.
 *
 * <p>Uses {@link MockMvc} with a mocked {@link IUserService} to validate the
 * controller contract for listing, retrieving, creating, updating, and
 * deactivating user profiles.</p>
 */
@SpringBootTest
class UserControllerTest {

    @Autowired private WebApplicationContext context;
    @Autowired private UserMapper userMapper;
    @MockitoBean private IUserService userService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    /**
     * Builds the {@link MockMvc} instance from the web application context
     * before each test case.
     */
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // ── GET /api/users/search ─────────────────────────────────────────────────

    @Test
    void search_noParams_returnsOkWithList() throws Exception {
        when(userService.search(null, null, null)).thenReturn(List.of(
                UserModel.builder().id(1L).fullName("Carlos").build(),
                UserModel.builder().id(2L).fullName("Ana").build()));

        mockMvc.perform(get("/api/users/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void search_withNameAndPosition_returnsFilteredList() throws Exception {
        when(userService.search("carlos", "FORWARD", null)).thenReturn(List.of(
                UserModel.builder().id(1L).fullName("Carlos").build()));

        mockMvc.perform(get("/api/users/search")
                        .param("name", "carlos")
                        .param("position", "FORWARD"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fullName").value("Carlos"));
    }

    @Test
    void search_withStatus_returnsFilteredList() throws Exception {
        when(userService.search(null, null, "ACTIVE")).thenReturn(List.of(
                UserModel.builder().id(3L).fullName("Luis").build()));

        mockMvc.perform(get("/api/users/search")
                        .param("status", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    // ── GET /api/users ────────────────────────────────────────────────────────

    @Test
    void getAll_returnsOkWithList() throws Exception {
        UserModel m1 = UserModel.builder().id(1L).fullName("Carlos").build();
        UserModel m2 = UserModel.builder().id(2L).fullName("Maria").build();
        when(userService.getAll()).thenReturn(List.of(m1, m2));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fullName").value("Carlos"));
    }

    // ── GET /api/users/{id} ──────────────────────────────────────────────────

    @Test
    void getById_found_returnsOkWithNoPassword() throws Exception {
        when(userService.getById(1L))
                .thenReturn(UserModel.builder().id(1L).fullName("Carlos")
                        .password("secret").email("c@eci.edu.co").build());

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.fullName").value("Carlos"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getById_notFound_returns404() throws Exception {
        when(userService.getById(99L))
                .thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not found"));
    }

    // ── GET /api/users/identification/{id} ───────────────────────────────────

    @Test
    void getByIdentification_found_returnsOk() throws Exception {
        when(userService.getByIdentification("12345"))
                .thenReturn(UserModel.builder().id(1L).identification("12345").build());

        mockMvc.perform(get("/api/users/identification/12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identification").value("12345"));
    }

    @Test
    void getByIdentification_notFound_returns404() throws Exception {
        when(userService.getByIdentification("xxx"))
                .thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(get("/api/users/identification/xxx"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/users ───────────────────────────────────────────────────────

    @Test
    void create_returnsCreated() throws Exception {
        UserRequest request = UserRequest.builder()
                .fullName("Ana").email("ana@eci.edu.co")
                .password("hash").identification("999")
                .build();
        when(userService.create(any()))
                .thenReturn(UserModel.builder().id(5L).fullName("Ana").build());

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    // ── PUT /api/users/{id} ───────────────────────────────────────────────────

    @Test
    void update_returnsOk() throws Exception {
        UserRequest request = UserRequest.builder().fullName("Nuevo nombre").build();
        when(userService.update(eq(1L), any()))
                .thenReturn(UserModel.builder().id(1L).fullName("Nuevo nombre").build());

        mockMvc.perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Nuevo nombre"));
    }

    @Test
    void update_notFound_returns404() throws Exception {
        when(userService.update(eq(99L), any()))
                .thenThrow(new ResourceNotFoundException("not found"));

        mockMvc.perform(put("/api/users/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                UserRequest.builder().fullName("x").build())))
                .andExpect(status().isNotFound());
    }

    // ── PUT /api/users/me ───────────────────────────────────────────────────

    @Test
    void updateMe_returnsOk() throws Exception {
        UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
                .fullName("Nuevo Nombre")
                .identification("123456")
                .birthDate(LocalDate.of(2001, 6, 10))
                .gender(Gender.MALE)
                .schoolRelation(SchoolRelation.STUDENT)
                .academicProgram("Ingenieria de Sistemas")
                .semester(6)
                .build();
        when(userService.updateProfile(eq(7L), any()))
                .thenReturn(UserModel.builder().id(7L).fullName("Nuevo Nombre").build());

        mockMvc.perform(put("/api/users/me")
                        .header("X-User-Id", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.fullName").value("Nuevo Nombre"));
    }

    @Test
    void updateMe_invalidPayload_returns400() throws Exception {
        UserProfileUpdateRequest request = UserProfileUpdateRequest.builder().build();

        mockMvc.perform(put("/api/users/me")
                        .header("X-User-Id", 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── PATCH /api/users/{id}/deactivate ─────────────────────────────────────

    @Test
    void deactivate_returnsNoContent() throws Exception {
        doNothing().when(userService).deactivate(1L);

        mockMvc.perform(patch("/api/users/1/deactivate"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deactivate_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("not found"))
                .when(userService).deactivate(99L);

        mockMvc.perform(patch("/api/users/99/deactivate"))
                .andExpect(status().isNotFound());
    }

    // ── PATCH /api/users/{id}/inactivate ───────────────────────────────────

    @Test
    void inactivate_returnsNoContent() throws Exception {
        doNothing().when(userService).inactivate(1L);

        mockMvc.perform(patch("/api/users/1/inactivate"))
                .andExpect(status().isNoContent());
    }

    @Test
    void inactivate_conflict_returns409() throws Exception {
        doThrow(new BusinessException("conflict"))
                .when(userService).inactivate(1L);

        mockMvc.perform(patch("/api/users/1/inactivate"))
                .andExpect(status().isConflict());
    }
}
