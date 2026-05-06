package edu.dosw.users.controller;

import edu.dosw.users.exception.ResourceNotFoundException;
import edu.dosw.users.model.PlayerPhoto;
import edu.dosw.users.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for serving player profile photos.
 *
 * <p>Base path: {@code /api/players/photos}</p>
 */
@RestController
@RequestMapping("/api/players/photos")
@RequiredArgsConstructor
public class PlayerPhotoController {

    private final ImageService imageService;

    /**
     * Returns the binary content of a player's profile photo.
     *
     * @param photoId MongoDB document identifier of the photo
     * @return image bytes with the correct {@code Content-Type}, or 404 if not found
     */
    @GetMapping("/{photoId}")
    public ResponseEntity<byte[]> getPhoto(@PathVariable String photoId) {
        PlayerPhoto photo = imageService.getPhoto(photoId);
        if (photo == null) {
            throw new ResourceNotFoundException("Photo not found with id: " + photoId);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.getContentType()))
                .body(photo.getData());
    }
}