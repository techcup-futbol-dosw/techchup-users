package edu.dosw.users.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Service for storing and deleting player photo files.
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
     * Deletes a previously stored photo.
     *
     * @param photoId identifier of the photo to delete
     */
    void delete(String photoId);
}
