package edu.dosw.users.repository;

import edu.dosw.users.entity.SportProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link SportProfileEntity} persistence operations.
 *
 * <p>Provides standard CRUD operations inherited from {@link JpaRepository} and
 * derived queries to retrieve sport profiles by user identifier and position.</p>
 *
 * @see SportProfileEntity
 */
public interface SportProfileRepository extends JpaRepository<SportProfileEntity, Long> {

    /**
     * Finds the sport profile associated with the given user.
     *
     * @param userId identifier of the user who owns the sport profile
     * @return optional containing the sport profile when it exists
     */
    Optional<SportProfileEntity> findByUserId(Long userId);

    /**
     * Finds all sport profiles with the given position.
     *
     * @param position position string to filter by (e.g. {@code "FORWARD"})
     * @return list of sport profiles with the requested position
     */
    List<SportProfileEntity> findByPosition(String position);

    /**
     * Finds all sport profiles matching the given availability flag.
     *
     * @param available {@code true} to return only available players
     * @return list of sport profiles with the requested availability
     */
    List<SportProfileEntity> findByAvailable(boolean available);

    /**
     * Finds all sport profiles matching both position and availability.
     *
     * @param position  position string to filter by
     * @param available availability flag to filter by
     * @return list of sport profiles matching both criteria
     */
    List<SportProfileEntity> findByPositionAndAvailable(String position, boolean available);
}
