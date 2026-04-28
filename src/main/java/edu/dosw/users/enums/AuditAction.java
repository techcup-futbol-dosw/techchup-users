package edu.dosw.users.enums;

/**
 * Action types that can be recorded in the system audit log.
 *
 * <p>Each value corresponds to a business event that modifies the state
 * of a sport profile or a team invitation.</p>
 */
public enum AuditAction {
    /** Creation of a sport profile or an invitation. */
    CREATE,
    /** Update of data belonging to a sport profile or invitation. */
    UPDATE,
    /** Deactivation of a sport profile (no physical deletion). */
    DEACTIVATE
}