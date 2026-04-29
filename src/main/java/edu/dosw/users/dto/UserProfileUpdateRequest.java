package edu.dosw.users.dto;

import edu.dosw.users.enums.Gender;
import edu.dosw.users.enums.SchoolRelation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Payload accepted for updating the current user's profile.
 *
 * <p>Deliberately excludes email and password, which are managed by the
 * identity service and cannot be modified through this endpoint.</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequest {

    @NotBlank
    private String fullName;

    @NotBlank
    private String identification;

    @NotNull
    @Past
    private LocalDate birthDate;

    @NotNull
    private Gender gender;

    @NotNull
    private SchoolRelation schoolRelation;

    @NotBlank
    private String academicProgram;

    @NotNull
    @Positive
    private Integer semester;
}
