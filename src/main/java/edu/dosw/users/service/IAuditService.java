package edu.dosw.users.service;

import edu.dosw.users.enums.AuditAction;
import edu.dosw.users.model.AuditLogModel;

import java.util.List;

/**
 * Service for recording and querying audit log entries.
 *
 * <p>Every relevant business event (creation, update, or deactivation of a
 * sport profile or invitation) must be recorded via this service so that
 * system history remains traceable.</p>
 */
public interface IAuditService {

    /**
     * Records an audit event associated with a sport profile.
     *
     * @param sportProfileId identifier of the affected sport profile
     * @param action         type of action performed
     * @param details        additional description of the event
     */
    void logSportProfile(Long sportProfileId, AuditAction action, String details);

    /**
     * Records an audit event associated with a team invitation.
     *
     * @param invitationId identifier of the affected invitation
     * @param action       type of action performed
     * @param details      additional description of the event
     */
    void logInvitation(Long invitationId, AuditAction action, String details);

    /**
     * Returns all audit log entries related to the given sport profile.
     *
     * @param sportProfileId identifier of the sport profile
     * @return list of matching audit log models, may be empty
     */
    List<AuditLogModel> getLogsForSportProfile(Long sportProfileId);

    /**
     * Returns all audit log entries related to the given invitation.
     *
     * @param invitationId identifier of the invitation
     * @return list of matching audit log models, may be empty
     */
    List<AuditLogModel> getLogsForInvitation(Long invitationId);
}