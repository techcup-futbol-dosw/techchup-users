package edu.dosw.users.controller;

import edu.dosw.users.model.UserProfileModel;
import edu.dosw.users.service.IUserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for user profile management.
 *
 * <p>Base path: {@code /api/users}</p>
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final IUserProfileService userProfileService;

    /** Returns all user profiles. */
    @GetMapping
    public ResponseEntity<List<UserProfileModel>> getAll() {
        return ResponseEntity.ok(userProfileService.getAll());
    }

    /** Returns the user profile with the given identifier. */
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileModel> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userProfileService.getById(id));
    }

    /** Returns the user profile with the given official identification number. */
    @GetMapping("/identification/{identification}")
    public ResponseEntity<UserProfileModel> getByIdentification(
            @PathVariable String identification) {
        return ResponseEntity.ok(userProfileService.getByIdentification(identification));
    }

    /** Creates a new user profile and returns it with status 201. */
    @PostMapping
    public ResponseEntity<UserProfileModel> create(@RequestBody UserProfileModel model) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userProfileService.create(model));
    }

    /** Replaces an existing user profile and returns the updated version. */
    @PutMapping("/{id}")
    public ResponseEntity<UserProfileModel> update(
            @PathVariable Long id, @RequestBody UserProfileModel model) {
        return ResponseEntity.ok(userProfileService.update(id, model));
    }

    /** Sets the profile status to INACTIVE. Returns 204 No Content. */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        userProfileService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}