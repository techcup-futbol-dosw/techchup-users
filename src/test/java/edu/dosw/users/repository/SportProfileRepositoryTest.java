package edu.dosw.users.repository;

import edu.dosw.users.entity.SportProfileEntity;
import edu.dosw.users.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link SportProfileRepository}.
 *
 * <p>Verifies the derived query used to locate a sport profile by the owning
 * user's identifier.</p>
 */
@SpringBootTest
@Transactional
class SportProfileRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SportProfileRepository repository;

    /**
     * Persists a user with unique identification and email values.
     *
     * @param identification identification number to assign to the user
     * @param email email address to assign to the user
     * @return saved user entity
     */
    private UserEntity savedUser(String identification, String email) {
        return userRepository.save(UserEntity.builder()
                .fullName("Test User")
                .email(email)
                .password("hashed")
                .identification(identification)
                .status("ACTIVE")
                .build());
    }

    /**
     * Persists a sport profile associated with the provided user.
     *
     * @param user owner of the sport profile
     * @return saved sport profile entity
     */
    private SportProfileEntity savedSportProfile(UserEntity user) {
        return repository.save(SportProfileEntity.builder()
                .user(user)
                .position("MIDFIELDER")
                .available(true)
                .build());
    }

    @Test
    void findByUser_Id_returnsSportProfile_whenExists() {
        UserEntity user = savedUser("12345678", "user@test.com");
        savedSportProfile(user);

        Optional<SportProfileEntity> result = repository.findByUser_Id(user.getId());

        assertTrue(result.isPresent());
        assertEquals(user.getId(), result.get().getUser().getId());
        assertEquals("MIDFIELDER", result.get().getPosition());
    }

    @Test
    void findByUser_Id_returnsEmpty_whenUserHasNoSportProfile() {
        UserEntity user = savedUser("12345678", "user@test.com");

        Optional<SportProfileEntity> result = repository.findByUser_Id(user.getId());

        assertFalse(result.isPresent());
    }

    @Test
    void findByUser_Id_returnsEmpty_whenUserDoesNotExist() {
        Optional<SportProfileEntity> result = repository.findByUser_Id(999L);

        assertFalse(result.isPresent());
    }
}
