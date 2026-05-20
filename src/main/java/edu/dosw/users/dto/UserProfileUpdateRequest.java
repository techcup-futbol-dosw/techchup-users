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
 * Payload aceptado para actualizar el perfil del usuario actual.
 *
 * <p>Excluye deliberadamente el correo electrónico y la contraseña, que son gestionados
 * por el servicio de identidad y no pueden modificarse a través de este endpoint.
 * Todos los campos están sujetos a validación con Bean Validation.</p>
 *
 * @author CodeForge
 * @since 1.0
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

    @Positive
    private Integer semester;
}
