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
 * Read-only view of a user profile returned by the API.
 *
 * <p>Deliberately omits the {@code password} field so that credentials are
 * never sent over the wire in a response.</p>
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

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
}