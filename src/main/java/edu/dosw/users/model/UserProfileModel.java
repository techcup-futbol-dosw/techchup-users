package edu.dosw.users.model;

import edu.dosw.users.enums.Gender;
import edu.dosw.users.enums.SchoolRelation;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Getter
@Setter
@Builder
public class UserProfileModel {

    private Long id;
    private String fullName;
    private String email;
    private String password;
    private String identification;
    private LocalDate birthDate;
    private Gender gender;
    private SchoolRelation schoolRelation;
    private String academicProgram;
    private Integer semester;
    private String status;
    private LocalDateTime profileCreatedAt;
    private LocalDateTime updatedAt;

    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }

    public int getAge() {
        if (birthDate == null) return 0;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}