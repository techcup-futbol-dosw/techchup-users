package edu.dosw.users.controller;

import edu.dosw.users.dto.SportProfileRequest;
import edu.dosw.users.dto.SportProfileResponse;
import edu.dosw.users.exception.ResourceNotFoundException;
import edu.dosw.users.mapper.SportProfileMapper;
import edu.dosw.users.model.PlayerPhoto;
import edu.dosw.users.service.ImageService;
import edu.dosw.users.service.ISportProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for sport profile management.
 *
 * <p>Base path: {@code /api/sport-profiles}</p>
 *
 * <p>Access control summary:
 * <ul>
 *   <li>Any authenticated user — may read any sport profile or photo.</li>
 *   <li>Profile owner (JUGADOR) — may create, update and toggle their own profile.</li>
 *   <li>ADMINISTRADOR — full access.</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/sport-profiles")
@RequiredArgsConstructor
public class SportProfileController {

    private final ISportProfileService sportProfileService;
    private final SportProfileMapper sportProfileMapper;
    private final ImageService imageService;

    /**
     * Returns the sport profile with the given identifier.
     * Readable by any authenticated user (captains searching for players, etc.).
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SportProfileResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                sportProfileMapper.toResponse(sportProfileService.getById(id)));
    }

    /**
     * Returns the sport profile associated with the given user.
     * Accessible by the profile owner, captains (player search) and administrators.
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("@sportProfileAccessPolicy.canAccessOwnSportProfile(#userId, authentication) or hasRole('CAPITAN') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<SportProfileResponse> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(
                sportProfileMapper.toResponse(sportProfileService.getByUserId(userId)));
    }

    /**
     * Creates a new sport profile for the specified user.
     * Only the owner can create their own sport profile.
     *
     * @param userId  owner user identifier (path)
     * @param request profile data as a JSON part
     * @param photo   optional player photo
     */
    @PostMapping(value = "/user/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@sportProfileAccessPolicy.canAccessOwnSportProfile(#userId, authentication) or hasRole('ADMINISTRADOR')")
    public ResponseEntity<SportProfileResponse> create(
            @PathVariable Long userId,
            @RequestPart("profile") SportProfileRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sportProfileMapper.toResponse(
                        sportProfileService.create(userId, sportProfileMapper.toModel(request), photo)));
    }

    /**
     * Updates an existing sport profile.
     * Only the profile owner may update; the service enforces the "not in active team" rule.
     *
     * @param id      identifier of the sport profile
     * @param request new profile data as a JSON part
     * @param photo   optional new photo; omitting it keeps the existing one
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@sportProfileAccessPolicy.canModifyOwnSportProfile(#id, authentication) or hasRole('ADMINISTRADOR')")
    public ResponseEntity<SportProfileResponse> update(
            @PathVariable Long id,
            @RequestPart("profile") SportProfileRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        return ResponseEntity.ok(
                sportProfileMapper.toResponse(
                        sportProfileService.update(id, sportProfileMapper.toModel(request), photo)));
    }

    /**
     * Sets the availability flag of the sport profile.
     * Only the profile owner may change their own availability.
     *
     * @param id        identifier of the sport profile
     * @param available new availability value (query param)
     */
    @PatchMapping("/{id}/availability")
    @PreAuthorize("@sportProfileAccessPolicy.canModifyOwnSportProfile(#id, authentication) or hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> updateAvailability(
            @PathVariable Long id,
            @RequestParam boolean available) {
        sportProfileService.updateAvailability(id, available);
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns the binary content of a player's profile photo.
     * Readable by any authenticated user.
     *
     * @param photoId MongoDB document identifier of the photo
     * @return image bytes with the correct {@code Content-Type}, or 404 if not found
     */
    @GetMapping("/photos/{photoId}")
    @PreAuthorize("isAuthenticated()")
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