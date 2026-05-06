package edu.dosw.users.service;

import edu.dosw.users.model.PlayerPhoto;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for storing, retrieving and deleting player photo files.
 *
 * <p>Implementations are responsible for persisting image bytes outside the
 * relational database and returning a photo identifier that can be referenced
 * from the sport profile.</p>
 */
public interface ImageService {

    /**
     * Stores the uploaded image for the given sport profile.
     *
     * @param file image file received from the client
     * @param sportProfileId identifier of the sport profile that owns the image
     * @return generated identifier of the stored photo
     * @throws java.io.UncheckedIOException if the file cannot be read
     */
    String upload(MultipartFile file, Long sportProfileId);

    /**
     * Retrieves a previously stored photo by its identifier.
     *
     * @param photoId identifier of the photo to retrieve
     * @return the {@link PlayerPhoto} document, or {@code null} if not found
     */
    PlayerPhoto getPhoto(String photoId);

    /**
     * Deletes a previously stored photo.
     *
     * @param photoId identifier of the photo to delete
     */
    void delete(String photoId);
}
