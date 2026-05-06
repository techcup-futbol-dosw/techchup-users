package edu.dosw.users.controller;

import edu.dosw.users.model.PlayerPhoto;
import edu.dosw.users.service.ImageService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Web layer tests for player photo endpoints.
 *
 * <p>Uses {@link MockMvc} with a mocked {@link ImageService} to verify binary
 * content delivery and exception-to-status mappings for
 * {@code GET /api/players/photos/{photoId}}.</p>
 */
@SpringBootTest
class PlayerPhotoControllerTest {

    @Autowired private WebApplicationContext context;
    @MockitoBean private ImageService imageService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    // ── GET /api/players/photos/{photoId} ────────────────────────────────────

    @Test
    void getPhoto_found_returnsBinaryWithContentType() throws Exception {
        PlayerPhoto photo = new PlayerPhoto();
        photo.setId("abc123");
        photo.setContentType("image/png");
        photo.setData(new byte[]{1, 2, 3});

        when(imageService.getPhoto("abc123")).thenReturn(photo);

        mockMvc.perform(get("/api/players/photos/abc123"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(new byte[]{1, 2, 3}));
    }

    @Test
    void getPhoto_notFound_returns404() throws Exception {
        when(imageService.getPhoto("nonexistent")).thenReturn(null);

        mockMvc.perform(get("/api/players/photos/nonexistent"))
                .andExpect(status().isNotFound());
    }
}