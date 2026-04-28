package edu.dosw.users.model;

import edu.dosw.users.enums.InvitationStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Domain model representing an invitation sent to a player to join a team
 * on the TechCup Fútbol platform.
 *
 * <p>Encapsulates the invitation lifecycle through the methods
 * {@link #accept()}, {@link #reject()} and {@link #cancel()}, which
 * automatically update the status and record the response timestamp.</p>
 *
 * @see edu.dosw.users.entity.InvitationEntity
 * @see edu.dosw.users.mapper.InvitationMapper
 * @see InvitationStatus
 */
@Getter
@Setter
@Builder
public class InvitationModel {

    /** Unique identifier of the invitation. */
    private Long id;
    /** Identifier of the player who receives the invitation. */
    private Long playerId;
    /** Identifier of the team that extends the invitation. */
    private Long teamId;
    /** Current status of the invitation in its lifecycle. */
    private InvitationStatus status;
    /** Date and time when the invitation was sent. */
    private LocalDateTime sentAt;
    /** Date and time when the player or captain responded to the invitation. */
    private LocalDateTime respondedAt;

    /**
     * Indicates whether the invitation is awaiting a response.
     *
     * @return {@code true} if the status is {@link InvitationStatus#PENDING}
     */
    public boolean isPending() {
        return InvitationStatus.PENDING == status;
    }

    /**
     * Accepts the invitation, changing the status to {@link InvitationStatus#ACCEPTED}
     * and recording the response timestamp.
     */
    public void accept() {
        this.status = InvitationStatus.ACCEPTED;
        this.respondedAt = LocalDateTime.now();
    }

    /**
     * Rejects the invitation, changing the status to {@link InvitationStatus#REJECTED}
     * and recording the response timestamp.
     */
    public void reject() {
        this.status = InvitationStatus.REJECTED;
        this.respondedAt = LocalDateTime.now();
    }

    /**
     * Cancels the invitation, changing the status to {@link InvitationStatus#CANCELLED}
     * and recording the cancellation timestamp.
     */
    public void cancel() {
        this.status = InvitationStatus.CANCELLED;
        this.respondedAt = LocalDateTime.now();
    }
}