package edu.dosw.users.enums;

/**
 * Género del usuario tal como está registrado en su perfil de la plataforma.
 *
 * <p>Se almacena como {@code String} en la base de datos relacional y se convierte
 * a este enum por MapStruct al mapear desde {@code UserEntity} a {@code UserModel}.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
public enum Gender {
    /** Masculino. */
    MALE,
    /** Femenino. */
    FEMALE,
    /** Otro género o no especificado. */
    OTHER
}