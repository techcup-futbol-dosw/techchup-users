package edu.dosw.users.repository;

import edu.dosw.users.entity.SportProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link SportProfileEntity} persistence operations.
 *
 * <p>Provides standard CRUD operations inherited from {@link JpaRepository} and
 * a derived query to retrieve a sport profile through its owning user.</p>
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
    Optional<SportProfileEntity> findByUser_Id(Long userId);
}
