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
 * Modelo de dominio que representa el perfil básico de un usuario registrado en la
 * plataforma TechCup Fútbol.
 *
 * <p>Almacena la información personal y la relación académica del usuario con la
 * institución. La autenticación y la gestión de contraseñas son responsabilidad
 * exclusiva del servicio de identidad.</p>
 *
 * @author CodeForge
 * @since 1.0
 * @see edu.dosw.users.mapper.UserMapper
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserModel {

    /** Identificador único del perfil de usuario. */
    private Long id;
    /** Nombre completo del usuario. */
    private String fullName;
    /** Dirección de correo electrónico institucional o personal del usuario. */
    private String email;
    /** Contraseña hasheada; gestionada por el servicio de identidad. */
    private String password;
    /** Número de identificación oficial del usuario. */
    private String identification;
    /** Fecha de nacimiento del usuario. */
    private LocalDate birthDate;
    /** Género del usuario. */
    private Gender gender;
    /** Tipo de relación entre el usuario y la institución educativa. */
    private SchoolRelation schoolRelation;
    /** Nombre del programa académico en el que está matriculado el usuario. */
    private String academicProgram;
    /** Semestre académico actual del usuario. */
    private Integer semester;
    /** Estado actual del perfil (p. ej. {@code "ACTIVE"}, {@code "INACTIVE"}). */
    private String status;
    /** Fecha y hora en que se creó el perfil. */
    private LocalDateTime profileCreatedAt;
    /** Fecha y hora de la última actualización del perfil. */
    private LocalDateTime updatedAt;

    /**
     * Indica si el perfil del usuario está actualmente activo.
     *
     * <p>La comparación es insensible a mayúsculas, por lo que tanto {@code "ACTIVE"}
     * como {@code "active"} retornan {@code true}.</p>
     *
     * @return {@code true} si el estado es igual a {@code "ACTIVE"} (en cualquier capitalización)
     */
    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(status);
    }

    /**
     * Calcula la edad actual del usuario en años completos a partir de {@link #birthDate}.
     *
     * @return edad en años, o {@code 0} si {@link #birthDate} es {@code null}
     */
    public int getAge() {
        if (birthDate == null) return 0;
        return Period.between(birthDate, LocalDate.now()).getYears();
    }
}