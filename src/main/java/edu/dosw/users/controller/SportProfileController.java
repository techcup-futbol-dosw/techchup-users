package edu.dosw.users.controller;

import edu.dosw.users.model.SportProfileModel;
import edu.dosw.users.service.ISportProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
 * <p>Create and update endpoints accept {@code multipart/form-data} so that
 * the profile JSON and the optional photo file can be sent together in a
 * single request.</p>
 */
@RestController
@RequestMapping("/api/sport-profiles")
@RequiredArgsConstructor
public class SportProfileController {

    private final ISportProfileService sportProfileService;

    /** Returns the sport profile with the given identifier. */
    @GetMapping("/{id}")
    public ResponseEntity<SportProfileModel> getById(@PathVariable Long id) {
        return ResponseEntity.ok(sportProfileService.getById(id));
    }

    /** Returns the sport profile associated with the given user. */
    @GetMapping("/user/{userId}")
    public ResponseEntity<SportProfileModel> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(sportProfileService.getByUserId(userId));
    }

    /**
     * Creates a new sport profile for the specified user.
     *
     * @param userId  owner user identifier (path)
     * @param profile profile data as a JSON part
     * @param photo   optional player photo
     */
    @PostMapping(value = "/user/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SportProfileModel> create(
            @PathVariable Long userId,
            @RequestPart("profile") SportProfileModel profile,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sportProfileService.create(userId, profile, photo));
    }

    /**
     * Updates an existing sport profile.
     *
     * @param id      identifier of the sport profile to update
     * @param profile new profile data as a JSON part
     * @param photo   optional new player photo; omitting it keeps the existing one
     */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SportProfileModel> update(
            @PathVariable Long id,
            @RequestPart("profile") SportProfileModel profile,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        return ResponseEntity.ok(sportProfileService.update(id, profile, photo));
    }

    /**
     * Sets the availability flag of the sport profile.
     *
     * @param id        identifier of the sport profile
     * @param available new availability value (query param)
     */
    @PatchMapping("/{id}/availability")
    public ResponseEntity<Void> updateAvailability(
            @PathVariable Long id,
            @RequestParam boolean available) {
        sportProfileService.updateAvailability(id, available);
        return ResponseEntity.noContent().build();
    }
}