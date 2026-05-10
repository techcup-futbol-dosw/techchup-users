package edu.dosw.users.controller;

import edu.dosw.users.dto.UserProfileUpdateRequest;
import edu.dosw.users.dto.AdminUserUpdateRequest;
import edu.dosw.users.dto.UserResponse;
import edu.dosw.users.mapper.UserMapper;
import edu.dosw.users.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
public class UserController {

    private final IUserService userService;
    private final UserMapper userMapper;

    /** Returns players matching the given filters (all parameters optional). */
    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(
                userService.search(name, position, status).stream()
                        .map(userMapper::toResponse)
                        .toList());
    }

    /** Returns all user profiles. */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(
                userService.getAll().stream()
                        .map(userMapper::toResponse)
                        .toList());
    }

    /** Returns the user profile with the given identifier. */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                userMapper.toResponse(userService.getById(id)));
    }

    /** Returns the user profile with the given official identification number. */
    @GetMapping("/identification/{identification}")
    public ResponseEntity<UserResponse> getByIdentification(
            @PathVariable String identification) {
        return ResponseEntity.ok(
                userMapper.toResponse(
                        userService.getByIdentification(identification)));
    }

    /** Replaces an existing user profile and returns the updated response. */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id, @RequestBody AdminUserUpdateRequest request) {
        return ResponseEntity.ok(
                userMapper.toResponse(
                        userService.update(id, userMapper.toModel(request))));
    }

    /** Updates the current user's profile. */
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        return ResponseEntity.ok(
                userMapper.toResponse(
                        userService.updateProfile(userId, userMapper.toModel(request))));
    }

    /** Sets the profile status to INACTIVE. Returns 204 No Content. */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        userService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    /** Inactivates the user profile after validating tournament participation. */
    @PatchMapping("/{id}/inactivate")
    public ResponseEntity<Void> inactivate(@PathVariable Long id) {
        userService.inactivate(id);
        return ResponseEntity.noContent().build();
    }
}