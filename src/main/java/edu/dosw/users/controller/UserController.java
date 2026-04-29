package edu.dosw.users.controller;

import edu.dosw.users.dto.UserRequest;
import edu.dosw.users.dto.UserResponse;
import edu.dosw.users.mapper.UserMapper;
import edu.dosw.users.service.IUserService;
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
public class UserController {

    private final IUserService userService;
    private final UserMapper userMapper;

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

    /** Creates a new user profile and returns 201 with the saved response. */
    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userMapper.toResponse(
                        userService.create(userMapper.toModel(request))));
    }

    /** Replaces an existing user profile and returns the updated response. */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(
            @PathVariable Long id, @RequestBody UserRequest request) {
        return ResponseEntity.ok(
                userMapper.toResponse(
                        userService.update(id, userMapper.toModel(request))));
    }

    /** Sets the profile status to INACTIVE. Returns 204 No Content. */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        userService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}