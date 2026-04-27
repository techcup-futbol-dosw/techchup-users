package edu.dosw.users.model;

import edu.dosw.users.enums.InvitationStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class InvitationModel {

    private Long id;
    private Long playerId;
    private Long teamId;
    private InvitationStatus status;
    private LocalDateTime sentAt;
    private LocalDateTime respondedAt;

    public boolean isPending() {
        return InvitationStatus.PENDING == status;
    }

    public void accept() {
        this.status = InvitationStatus.ACCEPTED;
        this.respondedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = InvitationStatus.REJECTED;
        this.respondedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = InvitationStatus.CANCELLED;
        this.respondedAt = LocalDateTime.now();
    }
}