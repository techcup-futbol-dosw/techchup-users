package edu.dosw.users.service;

import edu.dosw.users.model.InvitationModel;

import java.util.List;

/**
 * Service for managing team invitations sent to players.
 *
 * <p>Handles the full lifecycle of an invitation: sending, accepting,
 * rejecting, and cancelling. Every state change is recorded in the audit log.</p>
 */
public interface IInvitationService {

    /**
     * Retrieves an invitation by its identifier.
     *
     * @param id identifier of the invitation
     * @return the corresponding model
     * @throws edu.dosw.users.exception.ResourceNotFoundException if not found
     */
    InvitationModel getById(Long id);

    /**
     * Returns all invitations received by the given player.
     *
     * @param playerId identifier of the player
     * @return list of matching invitation models, may be empty
     */
    List<InvitationModel> getByPlayerId(Long playerId);

    /**
     * Sends a new invitation to a player from the specified team.
     *
     * @param playerId identifier of the player receiving the invitation
     * @param teamId   identifier of the team extending the invitation
     * @return the saved invitation model
     * @throws edu.dosw.users.exception.ResourceNotFoundException if the player does not exist
     * @throws edu.dosw.users.exception.BusinessException         if the player already has a pending invitation from the same team
     */
    InvitationModel send(Long playerId, Long teamId);

    /**
     * Accepts a pending invitation.
     *
     * @param id identifier of the invitation
     * @return the updated invitation model
     * @throws edu.dosw.users.exception.ResourceNotFoundException if not found
     * @throws edu.dosw.users.exception.BusinessException         if the invitation is not in {@code PENDING} status
     */
    InvitationModel accept(Long id);

    /**
     * Rejects a pending invitation.
     *
     * @param id identifier of the invitation
     * @return the updated invitation model
     * @throws edu.dosw.users.exception.ResourceNotFoundException if not found
     * @throws edu.dosw.users.exception.BusinessException         if the invitation is not in {@code PENDING} status
     */
    InvitationModel reject(Long id);

    /**
     * Cancels a pending invitation.
     *
     * @param id identifier of the invitation
     * @return the updated invitation model
     * @throws edu.dosw.users.exception.ResourceNotFoundException if not found
     * @throws edu.dosw.users.exception.BusinessException         if the invitation is not in {@code PENDING} status
     */
    InvitationModel cancel(Long id);
}