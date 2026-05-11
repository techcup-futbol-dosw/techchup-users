package edu.dosw.users.dto;

import edu.dosw.users.enums.Gender;
import edu.dosw.users.enums.SchoolRelation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Payload accepted for user profile creation and update requests.
 *
 * <p>Excludes internal fields such as {@code id}, {@code status} and
 * timestamps — those are managed exclusively by the service layer.</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    private String fullName;
    private String email;
    private String password;
    private String identification;
    private LocalDate birthDate;
    private Gender gender;
    private SchoolRelation schoolRelation;
    private String academicProgram;
    private Integer semester;
}