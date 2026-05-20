package edu.dosw.users.dto;

import edu.dosw.users.enums.Gender;
import edu.dosw.users.enums.SchoolRelation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for the player search endpoint ({@code GET /api/users/search}).
 *
 * <p>Combines user profile data from the identity service with sport profile
 * data stored locally. Sport profile fields ({@code position}, {@code dorsalNumber},
 * {@code available}) are {@code null} when the player has not yet created a sport profile.</p>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSearchResponse {

    // ── User fields ───────────────────────────────────────────────────────────
    private Long id;
    private String fullName;
    private String email;
    private String identification;
    private LocalDate birthDate;
    private Gender gender;
    private SchoolRelation schoolRelation;
    private String academicProgram;
    private Integer semester;
    private String status;
    private LocalDateTime profileCreatedAt;
    private LocalDateTime updatedAt;

    // ── Sport profile fields (null when no sport profile exists) ──────────────
    private String position;
    private Integer dorsalNumber;
    private Boolean available;
}