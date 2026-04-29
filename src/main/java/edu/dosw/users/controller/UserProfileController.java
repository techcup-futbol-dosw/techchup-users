package edu.dosw.users.controller;

import edu.dosw.users.dto.UserProfileRequest;
import edu.dosw.users.dto.UserProfileResponse;
import edu.dosw.users.mapper.UserProfileMapper;
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
    private final UserProfileMapper userProfileMapper;

    /** Returns all user profiles. */
    @GetMapping
    public ResponseEntity<List<UserProfileResponse>> getAll() {
        return ResponseEntity.ok(
                userProfileService.getAll().stream()
                        .map(userProfileMapper::toResponse)
                        .toList());
    }

    /** Returns the user profile with the given identifier. */
    @GetMapping("/{id}")
    public ResponseEntity<UserProfileResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                userProfileMapper.toResponse(userProfileService.getById(id)));
    }

    /** Returns the user profile with the given official identification number. */
    @GetMapping("/identification/{identification}")
    public ResponseEntity<UserProfileResponse> getByIdentification(
            @PathVariable String identification) {
        return ResponseEntity.ok(
                userProfileMapper.toResponse(
                        userProfileService.getByIdentification(identification)));
    }

    /** Creates a new user profile and returns 201 with the saved response. */
    @PostMapping
    public ResponseEntity<UserProfileResponse> create(@RequestBody UserProfileRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userProfileMapper.toResponse(
                        userProfileService.create(userProfileMapper.toModel(request))));
    }

    /** Replaces an existing user profile and returns the updated response. */
    @PutMapping("/{id}")
    public ResponseEntity<UserProfileResponse> update(
            @PathVariable Long id, @RequestBody UserProfileRequest request) {
        return ResponseEntity.ok(
                userProfileMapper.toResponse(
                        userProfileService.update(id, userProfileMapper.toModel(request))));
    }

    /** Sets the profile status to INACTIVE. Returns 204 No Content. */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        userProfileService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}