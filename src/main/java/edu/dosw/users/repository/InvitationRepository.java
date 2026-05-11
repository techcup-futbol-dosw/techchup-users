package edu.dosw.users.repository;

import edu.dosw.users.entity.InvitationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link InvitationEntity} persistence operations.
 *
 * <p>Includes the standard CRUD operations inherited from {@link JpaRepository}
 * and derived queries for retrieving invitations by user identifier and status.</p>
 *
 * @see InvitationEntity
 */
public interface InvitationRepository extends JpaRepository<InvitationEntity, Long> {

    /**
     * Finds all invitations received by the given user.
     *
     * @param userId identifier of the user whose invitations are requested
     * @return list of invitations associated with the user
     */
    List<InvitationEntity> findByUserId(Long userId);

    /**
     * Finds all invitations received by the given user with the requested status.
     *
     * @param userId identifier of the user whose invitations are requested
     * @param status invitation status to filter by
     * @return list of invitations associated with the user and matching status
     */
    List<InvitationEntity> findByUserIdAndStatus(Long userId, String status);
}
