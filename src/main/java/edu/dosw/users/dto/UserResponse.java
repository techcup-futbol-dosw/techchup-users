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
 * Vista de solo lectura de un perfil de usuario retornada por la API.
 *
 * <p>Omite deliberadamente el campo {@code password} para que las credenciales
 * nunca sean enviadas en una respuesta.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

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