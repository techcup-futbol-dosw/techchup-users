package edu.dosw.users.service;

import edu.dosw.users.model.PlayerPhoto;
import edu.dosw.users.repository.PlayerPhotoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ImageServiceImpl}.
 *
 * <p>Verifies that multipart files are transformed into {@link PlayerPhoto}
 * documents, that upload failures are propagated as runtime exceptions, and
 * that delete operations are delegated to {@link PlayerPhotoRepository}.</p>
 */
@ExtendWith(MockitoExtension.class)
class ImageServiceImplTest {

    @Mock
    private PlayerPhotoRepository playerPhotoRepository;

    @InjectMocks
    private ImageServiceImpl imageService;

    @Test
    void upload_savesPhotoWithCorrectFields_andReturnsId() {
        MockMultipartFile file = new MockMultipartFile(
                "photo", "perfil.jpg", "image/jpeg", "fake-image-bytes".getBytes()
        );
        PlayerPhoto savedPhoto = new PlayerPhoto();
        savedPhoto.setId("abc123def456abc123def456");
        when(playerPhotoRepository.save(any(PlayerPhoto.class))).thenReturn(savedPhoto);

        String result = imageService.upload(file, 1L);

        assertEquals("abc123def456abc123def456", result);
        ArgumentCaptor<PlayerPhoto> captor = ArgumentCaptor.forClass(PlayerPhoto.class);
        verify(playerPhotoRepository).save(captor.capture());
        PlayerPhoto captured = captor.getValue();
        assertEquals(1L, captured.getSportProfileId());
        assertEquals("image/jpeg", captured.getContentType());
        assertArrayEquals("fake-image-bytes".getBytes(), captured.getData());
        assertNotNull(captured.getUploadedAt());
    }

    @Test
    void upload_throwsRuntimeException_whenFileReadFails() throws Exception {
        MockMultipartFile file = mock(MockMultipartFile.class);
        when(file.getBytes()).thenThrow(new java.io.IOException("disco lleno"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> imageService.upload(file, 1L));

        assertTrue(ex.getMessage().contains("Error al leer el archivo de imagen"));
    }

    @Test
    void delete_callsRepositoryDeleteById() {
        imageService.delete("abc123def456abc123def456");

        verify(playerPhotoRepository).deleteById("abc123def456abc123def456");
    }
}
