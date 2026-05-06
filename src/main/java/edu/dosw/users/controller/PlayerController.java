package edu.dosw.users.controller;

import edu.dosw.users.dto.SportProfileResponse;
import edu.dosw.users.mapper.SportProfileMapper;
import edu.dosw.users.service.ISportProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller exposing player-centric endpoints.
 *
 * <p>Base path: {@code /api/players}</p>
 */
@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerController {

    private final ISportProfileService sportProfileService;
    private final SportProfileMapper sportProfileMapper;

    /**
     * Returns the sport profile of the player with the given identifier.
     *
     * @param id user identifier of the player
     * @return sport profile, or 404 if no profile exists for that player
     */
    @GetMapping("/{id}/profile")
    public ResponseEntity<SportProfileResponse> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(
                sportProfileMapper.toResponse(sportProfileService.getByUserId(id)));
    }
}