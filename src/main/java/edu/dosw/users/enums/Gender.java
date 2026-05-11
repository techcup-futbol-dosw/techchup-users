package edu.dosw.users.enums;

/**
 * Gender of the user as recorded in their platform profile.
 *
 * <p>Stored as a {@code String} in the relational database and converted
 * to this enum by MapStruct when mapping from {@code UserEntity}
 * to {@code UserModel}.</p>
 */
public enum Gender {
    /** Male. */
    MALE,
    /** Female. */
    FEMALE,
    /** Other gender or not specified. */
    OTHER
}