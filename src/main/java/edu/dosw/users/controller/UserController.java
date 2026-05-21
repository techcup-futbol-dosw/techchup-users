package edu.dosw.users.controller;

import edu.dosw.users.dto.UserProfileUpdateRequest;
import edu.dosw.users.dto.AdminUserUpdateRequest;
import edu.dosw.users.dto.PlayerSearchResponse;
import edu.dosw.users.dto.UserResponse;
import edu.dosw.users.mapper.UserMapper;
import edu.dosw.users.service.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
 *
 * <p>Access control summary:
 * <ul>
 *   <li>ADMIN — full access to all endpoints.</li>
 *   <li>CAPITAN — can search players by filter and look up by identification.</li>
 *   <li>Any authenticated user — can read and update their own profile.</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;
    private final UserMapper userMapper;

    /**
     * Returns players matching the given filters.
     * Only captains and admins may search for players (per project requirements).
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('CAPITAN') or hasRole('ADMIN')")
    public ResponseEntity<List<PlayerSearchResponse>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String identification,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) Integer age,
            @RequestParam(required = false) Boolean available) {
        return ResponseEntity.ok(
                userService.searchPlayers(name, position, status, identification, gender, semester, age, available));
    }

    /**
     * Returns all user profiles.
     * Restricted to administrators only.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAll() {
        List<UserResponse> users = userService.getAll().stream()
                .map(userMapper::toResponse)
                .toList();
        return ResponseEntity.ok(users);
    }

    /**
     * Returns the user profile with the given identifier.
     * Accessible by the owner or an administrator.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userAccessPolicy.canAccessOwnUser(#id, authentication)")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                userMapper.toResponse(userService.getById(id)));
    }

    /**
     * Returns the user profile with the given official identification number.
     * Captains use this when building their roster; admins have full access.
     */
    @GetMapping("/identification/{identification}")
    @PreAuthorize("hasRole('CAPITAN') or hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getByIdentification(
            @PathVariable String identification) {
        return ResponseEntity.ok(
                userMapper.toResponse(
                        userService.getByIdentification(identification)));
    }

    /**
     * Replaces an existing user profile (admin operation).
     * Only administrators can perform full user updates.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id, @RequestBody AdminUserUpdateRequest request) {
        return ResponseEntity.ok(
                userMapper.toResponse(
                        userService.update(id, userMapper.toModel(request))));
    }

    /**
     * Updates the current user's own profile.
     * Any authenticated user may update their own basic information.
     */
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateMe(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        return ResponseEntity.ok(
                userMapper.toResponse(
                        userService.updateProfile(userId, userMapper.toModel(request))));
    }

    /**
     * Deactivates a user account (admin operation).
     * Only administrators can forcibly deactivate any account.
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        userService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Inactivates the user's own account after validating tournament participation.
     * The user may inactivate their own account; admins may inactivate any account.
     */
    @PatchMapping("/{id}/inactivate")
    @PreAuthorize("hasRole('ADMIN') or @userAccessPolicy.canAccessOwnUser(#id, authentication)")
    public ResponseEntity<Void> inactivate(@PathVariable Long id) {
        userService.inactivate(id);
        return ResponseEntity.noContent().build();
    }
}