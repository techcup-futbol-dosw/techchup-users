package edu.dosw.users.dto;

import edu.dosw.users.enums.SchoolRelation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * DTO accepted by administrators when updating a user's basic information.
 *
 * <p>Contains only the fields that an administrator is allowed to change.</p>
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserUpdateRequest implements Serializable {

    private String fullName;

    private SchoolRelation schoolRelation;

    private String academicProgram;

    private Integer semester;
}
