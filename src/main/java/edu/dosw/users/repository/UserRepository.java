package edu.dosw.users.repository;

import edu.dosw.users.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link UserEntity} persistence operations.
 *
 * <p>Provides the standard CRUD operations inherited from {@link JpaRepository}
 * and a derived query for locating users by their official identification
 * number.</p>
 *
 * @see UserEntity
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * Finds a user by their unique official identification number.
     *
     * @param identification official identification number of the user
     * @return optional containing the user when a matching identification exists
     */
    Optional<UserEntity> findByIdentification(String identification);
}
