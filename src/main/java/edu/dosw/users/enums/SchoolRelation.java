package edu.dosw.users.enums;

/**
 * Type of relationship between the user and Escuela Colombiana de Ingeniería
 * Julio Garavito.
 *
 * <p>Defines the institutional link of the player and may be used
 * for eligibility validation in internal tournaments.</p>
 */
public enum SchoolRelation {
    /** Active student of the institution. */
    STUDENT,
    /** Professor affiliated with the institution. */
    PROFESSOR,
    /** Administrative staff of the institution. */
    ADMINISTRATIVE,
    /** Graduate of an academic programme at the institution. */
    GRADUATE,
    /** Family member of an institutional community member. */
    FAMILY
}