package edu.dosw.users.controller;

import edu.dosw.users.dto.SportProfileRequest;
import edu.dosw.users.dto.SportProfileResponse;
import edu.dosw.users.mapper.SportProfileMapper;
import edu.dosw.users.service.ISportProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for player-specific sport profile operations.
 *
 * <p>Base path: {@code /api/players}</p>
 */
@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerProfileController {

    private final ISportProfileService sportProfileService;
    private final SportProfileMapper sportProfileMapper;

    /** Updates the current player's sport profile. */
    @PutMapping(value = "/me/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SportProfileResponse> updateMyProfile(
            @RequestHeader("X-User-Id") Long userId,
            @RequestPart("profile") SportProfileRequest request,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        Long profileId = sportProfileService.getByUserId(userId).getId();
        return ResponseEntity.ok(
                sportProfileMapper.toResponse(
                        sportProfileService.update(
                                profileId, sportProfileMapper.toModel(request), photo)));
    }
}
