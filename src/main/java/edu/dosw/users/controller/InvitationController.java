package edu.dosw.users.controller;

import edu.dosw.users.dto.InvitationResponse;
import edu.dosw.users.mapper.InvitationMapper;
import edu.dosw.users.service.IInvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for team invitation management.
 *
 * <p>Base path: {@code /api/invitations}</p>
 */
@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final IInvitationService invitationService;
    private final InvitationMapper invitationMapper;

    /** Returns the invitation with the given identifier. */
    @GetMapping("/{id}")
    public ResponseEntity<InvitationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                invitationMapper.toResponse(invitationService.getById(id)));
    }

    /** Returns all invitations received by the given player. */
    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<InvitationResponse>> getByPlayerId(@PathVariable Long playerId) {
        return ResponseEntity.ok(
                invitationService.getByPlayerId(playerId).stream()
                        .map(invitationMapper::toResponse)
                        .toList());
    }

    /** Sends a new invitation. Returns 201 Created. */
    @PostMapping("/player/{playerId}/team/{teamId}")
    public ResponseEntity<InvitationResponse> send(
            @PathVariable Long playerId,
            @PathVariable Long teamId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invitationMapper.toResponse(invitationService.send(playerId, teamId)));
    }

    /** Accepts a pending invitation. */
    @PatchMapping("/{id}/accept")
    public ResponseEntity<InvitationResponse> accept(@PathVariable Long id) {
        return ResponseEntity.ok(
                invitationMapper.toResponse(invitationService.accept(id)));
    }

    /** Rejects a pending invitation. */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<InvitationResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(
                invitationMapper.toResponse(invitationService.reject(id)));
    }

    /** Cancels a pending invitation. */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<InvitationResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(
                invitationMapper.toResponse(invitationService.cancel(id)));
    }
}