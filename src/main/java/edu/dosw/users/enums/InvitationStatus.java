package edu.dosw.users.enums;

/**
 * Possible states during the lifecycle of a team invitation.
 *
 * <p>State transitions are performed through the methods
 * {@code InvitationModel#accept()}, {@code InvitationModel#reject()} and
 * {@code InvitationModel#cancel()}.</p>
 */
public enum InvitationStatus {
    /** The invitation has been sent and has not yet received a response from the player. */
    PENDING,
    /** The player accepted the invitation and joined the team. */
    ACCEPTED,
    /** The player declined the invitation. */
    REJECTED,
    /** The captain or administrator cancelled the invitation before it was answered. */
    CANCELLED
}