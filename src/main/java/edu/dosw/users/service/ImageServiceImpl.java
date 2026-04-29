package edu.dosw.users.service;

import edu.dosw.users.model.PlayerPhoto;
import edu.dosw.users.repository.PlayerPhotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDateTime;

/**
 * MongoDB-backed implementation of {@link ImageService}.
 *
 * <p>The service is only registered when a {@link MongoTemplate} bean is
 * available, allowing the application to start with an alternative fallback
 * when MongoDB is not configured.</p>
 *
 * @see PlayerPhotoRepository
 * @see PlayerPhoto
 */
@Service
@RequiredArgsConstructor
@ConditionalOnBean(MongoTemplate.class)
public class ImageServiceImpl implements ImageService {

    private final PlayerPhotoRepository playerPhotoRepository;

    /**
     * {@inheritDoc}
     *
     * <p>Reads the multipart file into a {@link PlayerPhoto} document and
     * stores its content type, binary data, owner sport profile identifier,
     * and upload timestamp.</p>
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
    public void delete(String photoId) {
        playerPhotoRepository.deleteById(photoId);
    }
}
