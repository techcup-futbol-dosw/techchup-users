package edu.dosw.users.enums;

/**
 * Playing positions available for a football player.
 *
 * <p>Used in {@code SportProfileModel} and {@code SportProfileEntity}
 * to classify the player's tactical role on the field.</p>
 */
public enum Position {
    /** Goalkeeper — responsible for defending the team's goal. */
    GOALKEEPER,
    /** Defender — defensive position close to the own goal. */
    DEFENDER,
    /** Midfielder — link between defence and attack. */
    MIDFIELDER,
    /** Forward — offensive position responsible for scoring goals. */
    FORWARD
}