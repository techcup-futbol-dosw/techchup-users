package edu.dosw.users.entity;

import edu.dosw.users.enums.Gender;
import edu.dosw.users.enums.SchoolRelation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "identification", unique = true)
    private String identification;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "school_relation")
    private SchoolRelation schoolRelation;

    @Column(name = "academic_program")
    private String academicProgram;

    @Column(name = "semester")
    private Integer semester;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "profile_created_at")
    private LocalDateTime profileCreatedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
