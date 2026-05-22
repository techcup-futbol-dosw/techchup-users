package edu.dosw.users.config;

import edu.dosw.users.service.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link FallbackBeansConfig}.
 *
 * <p>Exercises the image service stub bean directly — without a Spring context.</p>
 */
class FallbackBeansConfigTest {

    private ImageService imageService;

    @BeforeEach
    void setUp() {
        FallbackBeansConfig config = new FallbackBeansConfig();
        imageService = config.imageServiceStub();
    }

    @Test
    void imageStub_upload_returnsNull() {
        assertNull(imageService.upload(null, 1L));
    }

    @Test
    void imageStub_getPhoto_returnsNull() {
        assertNull(imageService.getPhoto("any-id"));
    }

    @Test
    void imageStub_delete_isNoOp() {
        assertDoesNotThrow(() -> imageService.delete("any-id"));
    }
}
