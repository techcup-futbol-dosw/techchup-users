package edu.dosw.users.controller;

import edu.dosw.users.model.InvitationModel;
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

    /** Returns the invitation with the given identifier. */
    @GetMapping("/{id}")
    public ResponseEntity<InvitationModel> getById(@PathVariable Long id) {
        return ResponseEntity.ok(invitationService.getById(id));
    }

    /** Returns all invitations received by the given player. */
    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<InvitationModel>> getByPlayerId(@PathVariable Long playerId) {
        return ResponseEntity.ok(invitationService.getByPlayerId(playerId));
    }

    /**
     * Sends a new invitation to the specified player from the specified team.
     * Returns 201 Created with the saved invitation.
     */
    @PostMapping("/player/{playerId}/team/{teamId}")
    public ResponseEntity<InvitationModel> send(
            @PathVariable Long playerId,
            @PathVariable Long teamId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invitationService.send(playerId, teamId));
    }

    /** Accepts a pending invitation. */
    @PatchMapping("/{id}/accept")
    public ResponseEntity<InvitationModel> accept(@PathVariable Long id) {
        return ResponseEntity.ok(invitationService.accept(id));
    }

    /** Rejects a pending invitation. */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<InvitationModel> reject(@PathVariable Long id) {
        return ResponseEntity.ok(invitationService.reject(id));
    }

    /** Cancels a pending invitation. */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<InvitationModel> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(invitationService.cancel(id));
    }
}