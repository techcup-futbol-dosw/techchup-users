package edu.dosw.users.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.dosw.users.dto.InvitationResponse;
import edu.dosw.users.enums.InvitationStatus;
import edu.dosw.users.mapper.InvitationMapper;
import edu.dosw.users.service.IInvitationService;
import lombok.RequiredArgsConstructor;

/**
 * REST controller for player invitation queries.
 *
 * <p>Base path: {@code /api/players}</p>
 */
@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
public class PlayerInvitationController {

    private final IInvitationService invitationService;
    private final InvitationMapper invitationMapper;

    /** Returns invitations received by the current player. */
    @GetMapping("/me/invitations")
    public ResponseEntity<List<InvitationResponse>> getMyInvitations(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) InvitationStatus status) {
        return ResponseEntity.ok(
                invitationService.getByPlayerId(userId, status).stream()
                        .map(invitationMapper::toResponse)
                        .toList());
    }
}
