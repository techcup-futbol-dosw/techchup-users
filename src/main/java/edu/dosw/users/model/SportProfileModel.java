package edu.dosw.users.model;

import edu.dosw.users.enums.Position;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Domain model representing a player's sport profile.
 *
 * <p>Contains the player's field position, jersey number, reference to the
 * photo document stored in MongoDB, and the player's availability to be
 * called up. It cannot be deleted or modified while the player is assigned
 * to an active team.</p>
 *
 * @see edu.dosw.users.entity.SportProfileEntity
 * @see edu.dosw.users.mapper.SportProfileMapper
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SportProfileModel {

    /** Unique identifier of the sport profile. */
    private Long id;
    /** Identifier of the user who owns this sport profile. */
    private Long userId;
    /** Player's tactical position on the field. */
    private Position position;
    /** Player's jersey number. */
    private Integer dorsalNumber;
    /** Identifier of the {@code PlayerPhoto} document in MongoDB. */
    private String photoId;
    /** Indicates whether the player is available to participate in tournaments. */
    private boolean available;
    /** Date and time when the sport profile was created. */
    private LocalDateTime createdAt;
    /** Date and time of the last update to the sport profile. */
    private LocalDateTime updatedAt;
}