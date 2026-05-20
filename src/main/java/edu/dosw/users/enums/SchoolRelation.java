package edu.dosw.users.enums;

/**
 * Tipo de relación entre el usuario y la Escuela Colombiana de Ingeniería
 * Julio Garavito.
 *
 * <p>Define el vínculo institucional del jugador y puede usarse para la
 * validación de elegibilidad en torneos internos.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
public enum SchoolRelation {
    /** Estudiante activo de la institución. */
    STUDENT,
    /** Docente vinculado a la institución. */
    PROFESSOR,
    /** Personal administrativo de la institución. */
    ADMINISTRATIVE,
    /** Egresado de un programa académico de la institución. */
    GRADUATE,
    /** Familiar de un miembro de la comunidad institucional. */
    FAMILY
}