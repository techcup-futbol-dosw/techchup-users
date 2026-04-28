package edu.dosw.users.client;

/**
 * Client interface for communicating with the teams microservice.
 *
 * <p>Used by the sport profile and invitation services to query team
 * membership before performing operations that are restricted while a player
 * is assigned to an active team.</p>
 */
public interface TeamsServiceClient {

    /**
     * Checks whether the given user is currently assigned to any team.
     *
     * @param userId identifier of the user to check
     * @return {@code true} if the user belongs to at least one active team
     */
    boolean isPlayerAssignedToTeam(Long userId);
}