package edu.dosw.users.model;

import edu.dosw.users.enums.Gender;
import edu.dosw.users.enums.SchoolRelation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

/**
 * Domain model representing the basic profile of a user registered on the
 * TechCup Fútbol platform.
 *
 * <p>Stores personal information and the user's academic relationship with the
 * institution. Authentication and password management are the exclusive
 * responsibility of the identity service.</p>
 *
 * @see edu.dosw.users.entity.UserProfileEntity
 * @see edu.dosw.users.mapper.UserProfileMapper
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileModel {

    /** Unique identifier of the user profile. */
    private Long id;
    /** Full name of the user. */
    private String fullName;
    /** Institutional or personal email address of the user. */
    private String email;
    /** Hashed password; managed by the identity service. */
    private String password;
    /** Official identification number of the user. */
    private String identification;
    /** Date of birth of the user. */
    private LocalDate birthDate;
    /** Gender of the user. */
    private Gender gender;
    /** Type of relationship between the user and the educational institution. */
    private SchoolRelation schoolRelation;
    /** Name of the academic programme in which the user is enrolled. */
    private String academicProgram;
    /** Current academic semester of the user. */
    private Integer semester;
    /** Current profile status (e.g. {@code "ACTIVE"}, {@code "INACTIVE"}). */
    private String status;
    /** Date and time when the profile was created. */
    private LocalDateTime profileCreatedAt;
    /** Date and time of the last profile update. */
    private LocalDateTime updatedAt;

    /**
     * Indicates whether the user's profile is currently active.
     *
     * <p>The comparison is case-insensitive, so both {@code "ACTIVE"} and
     * {@code "active"} return {@code true}.</p>
     *
     * @return {@code true} if the status equals {@code "ACTIVE"} (any casing)
     */
    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }

    /**
     * Calculates the user's current age in full years from {@link #birthDate}.
     *
     * @return age in years, or {@code 0} if {@link #birthDate} is {@code null}
     */
    public int getAge() {
        if (birthDate == null) return 0;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}