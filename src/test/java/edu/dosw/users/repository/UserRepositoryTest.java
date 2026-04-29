package edu.dosw.users.repository;

import edu.dosw.users.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link UserRepository}.
 *
 * <p>Verifies that users can be located by their unique identification number
 * and that missing identifiers return an empty result.</p>
 */
@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    /**
     * Builds a user entity with the required fields for persistence tests.
     *
     * @param identification identification number to assign
     * @param email email address to assign
     * @return unsaved user entity
     */
    private UserEntity buildUser(String identification, String email) {
        return UserEntity.builder()
                .fullName("Test User")
                .email(email)
                .password("hashed")
                .identification(identification)
                .status("ACTIVE")
                .build();
    }

    @Test
    void findByIdentification_returnsUser_whenExists() {
        repository.save(buildUser("12345678", "user@test.com"));

        Optional<UserEntity> result = repository.findByIdentification("12345678");

        assertTrue(result.isPresent());
        assertEquals("12345678", result.get().getIdentification());
    }

    @Test
    void findByIdentification_returnsEmpty_whenNotExists() {
        Optional<UserEntity> result = repository.findByIdentification("99999999");

        assertFalse(result.isPresent());
    }

    @Test
    void findByIdentification_returnsCorrectUser_whenMultipleExist() {
        repository.save(buildUser("11111111", "user1@test.com"));
        repository.save(buildUser("22222222", "user2@test.com"));

        Optional<UserEntity> result = repository.findByIdentification("22222222");

        assertTrue(result.isPresent());
        assertEquals("22222222", result.get().getIdentification());
    }
}
