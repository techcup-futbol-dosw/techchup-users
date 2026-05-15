package edu.dosw.users.controller;

import edu.dosw.users.dto.InvitationResponse;
import edu.dosw.users.mapper.InvitationMapper;
import edu.dosw.users.service.IInvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
 *
 * <p>Access control summary:
 * <ul>
 *   <li>CAPITAN — can send and cancel invitations.</li>
 *   <li>JUGADOR (owner) — can view, accept and reject their own invitations.</li>
 *   <li>ADMINISTRADOR — full access to all invitation endpoints.</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final IInvitationService invitationService;
    private final InvitationMapper invitationMapper;

    /**
     * Returns the invitation with the given identifier.
     * Accessible by any authenticated user; service layer enforces ownership.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InvitationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                invitationMapper.toResponse(invitationService.getById(id)));
    }

    /**
     * Returns all invitations received by the given user.
     * Only the invitation owner or an administrator may list them.
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("@invitationAccessPolicy.canAccessOwnInvitation(#userId, authentication) or hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<InvitationResponse>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(
                invitationService.getByPlayerId(userId).stream()
                        .map(invitationMapper::toResponse)
                        .toList());
    }

    /**
     * Sends a new invitation from a team to a player.
     * Only captains may send invitations (they manage their team roster).
     */
    @PostMapping("/user/{userId}/team/{teamId}")
    @PreAuthorize("hasRole('CAPITAN')")
    public ResponseEntity<InvitationResponse> send(
            @PathVariable Long userId,
            @PathVariable Long teamId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invitationMapper.toResponse(invitationService.send(userId, teamId)));
    }

    /**
     * Accepts a pending invitation.
     * Only the invited player (owner) or an administrator may accept.
     */
    @PatchMapping("/{id}/accept")
    @PreAuthorize("@invitationAccessPolicy.canRespondToInvitation(#id, authentication) or hasRole('ADMINISTRADOR')")
    public ResponseEntity<InvitationResponse> accept(@PathVariable Long id) {
        return ResponseEntity.ok(
                invitationMapper.toResponse(invitationService.accept(id)));
    }

    /**
     * Rejects a pending invitation.
     * Only the invited player (owner) or an administrator may reject.
     */
    @PatchMapping("/{id}/reject")
    @PreAuthorize("@invitationAccessPolicy.canRespondToInvitation(#id, authentication) or hasRole('ADMINISTRADOR')")
    public ResponseEntity<InvitationResponse> reject(@PathVariable Long id) {
        return ResponseEntity.ok(
                invitationMapper.toResponse(invitationService.reject(id)));
    }

    /**
     * Cancels a pending invitation.
     * Only the captain who sent it may cancel it.
     */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasRole('CAPITAN')")
    public ResponseEntity<InvitationResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(
                invitationMapper.toResponse(invitationService.cancel(id)));
    }
}