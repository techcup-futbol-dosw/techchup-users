package edu.dosw.users.repository;

import edu.dosw.users.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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

    @Query("SELECT u FROM UserEntity u JOIN u.sportProfile sp " +
           "WHERE (:name IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:status IS NULL OR u.status = :status) " +
           "AND (:position IS NULL OR sp.position = :position)")
    List<UserEntity> searchPlayers(@Param("name") String name,
                                   @Param("status") String status,
                                   @Param("position") String position);
}
