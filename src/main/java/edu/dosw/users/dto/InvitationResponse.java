package edu.dosw.users.dto;

import edu.dosw.users.enums.InvitationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Vista de solo lectura de una invitación de equipo retornada por la API.
 *
 * @author CodeForge
 * @since 1.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationResponse {

    private Long id;
    private Long playerId;
    private Long teamId;
    private InvitationStatus status;
    private LocalDateTime sentAt;
    private LocalDateTime respondedAt;
}