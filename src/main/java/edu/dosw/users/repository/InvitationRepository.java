package edu.dosw.users.repository;

import edu.dosw.users.entity.InvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link InvitationEntity} persistence operations.
 *
 * <p>Includes the standard CRUD operations inherited from {@link JpaRepository}
 * and derived queries for retrieving invitations by player and status.</p>
 *
 * @see InvitationEntity
 */
public interface InvitationRepository extends JpaRepository<InvitationEntity, Long> {

    /**
     * Finds all invitations received by the given player.
     *
     * @param playerId identifier of the player whose invitations are requested
     * @return list of invitations associated with the player
     */
    List<InvitationEntity> findByPlayer_Id(Long playerId);

    /**
     * Finds all invitations received by the given player with the requested status.
     *
     * @param playerId identifier of the player whose invitations are requested
     * @param status invitation status to filter by
     * @return list of invitations associated with the player and matching status
     */
    List<InvitationEntity> findByPlayer_IdAndStatus(Long playerId, String status);
}
