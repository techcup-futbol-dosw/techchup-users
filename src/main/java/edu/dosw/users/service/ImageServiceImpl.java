package edu.dosw.users.service;

import edu.dosw.users.model.PlayerPhoto;
import edu.dosw.users.repository.PlayerPhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;

/**
 * Implementación de {@link ImageService} respaldada por MongoDB.
 *
 * <p>Activa únicamente en el perfil {@code prod}, donde está configurado MongoDB Atlas.
 * En otros perfiles se utiliza el stub definido en {@code FallbackBeansConfig}.</p>
 *
 * @author CodeForge
 * @since 1.0
 * @see PlayerPhotoRepository
 * @see PlayerPhoto
 */
@Service
@RequiredArgsConstructor
@Profile("prod")
public class ImageServiceImpl implements ImageService {

    private final PlayerPhotoRepository playerPhotoRepository;

    /**
     * {@inheritDoc}
     *
     * <p>Lee el archivo multipart hacia un documento {@link PlayerPhoto} y almacena
     * su tipo de contenido, datos binarios, identificador del perfil deportivo
     * propietario y marca de tiempo de subida.</p>
     */
    @Override
    public String upload(MultipartFile file, Long sportProfileId) {
        try {
            PlayerPhoto photo = new PlayerPhoto();
            photo.setSportProfileId(sportProfileId);
            photo.setContentType(file.getContentType());
            photo.setData(file.getBytes());
            photo.setUploadedAt(LocalDateTime.now());
            return playerPhotoRepository.save(photo).getId();
        } catch (IOException e) {
            throw new UncheckedIOException("Error al leer el archivo de imagen", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerPhoto getPhoto(String photoId) {
        return playerPhotoRepository.findById(photoId).orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(String photoId) {
        playerPhotoRepository.deleteById(photoId);
    }
}
