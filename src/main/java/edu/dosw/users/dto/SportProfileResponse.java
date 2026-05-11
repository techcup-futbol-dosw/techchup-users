package edu.dosw.users.dto;

import edu.dosw.users.enums.Position;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Read-only view of a sport profile returned by the API.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SportProfileResponse {

    private Long id;
    private Long userId;
    private Position position;
    private Integer dorsalNumber;
    private String photoId;
    private boolean available;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}