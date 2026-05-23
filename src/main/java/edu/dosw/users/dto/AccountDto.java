package edu.dosw.users.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO local que mapea la respuesta del Identity Service para cuentas de usuario.
 *
 * <p>Cubre tanto la respuesta completa de {@code GET /accounts/{id}} como los
 * elementos de la lista paginada {@code GET /accounts}. Los campos ausentes en
 * la respuesta de lista (birthDate, gender, relation) quedan como {@code null}.</p>
 *
 * <p>Los enums del Identity Service se reciben como {@code String} para evitar
 * acoplamiento en tiempo de compilación con ese servicio.</p>
 */
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountDto {

    private Long id;

    /** Nombre de pila. Presente en ambas respuestas. */
    private String name;

    /** Apellido. Presente en ambas respuestas. */
    private String lastName;

    /**
     * Nombre completo ya concatenado. Presente en la respuesta de lista
     * ({@code AccountAdminItemResponse}); ausente en la respuesta individual
     * ({@code AccountResponse}).
     */
    private String fullName;

    private String email;

    /** {@code "ACTIVE"} o {@code "INACTIVE"}. */
    private String status;

    private LocalDate birthDate;

    /** {@code "MALE"}, {@code "FEMALE"} o {@code "OTHER"}. */
    private String gender;

    /**
     * Relación con la institución: {@code "ESTUDIANTE"}, {@code "GRADUADO"},
     * {@code "PROFESOR"}, {@code "PERSONAL_ADMIN"}, {@code "FAMILIAR"}, etc.
     */
    private String relation;

    private Integer semester;

    private String program;

    private String identification;

    private LocalDateTime createdAt;
}
