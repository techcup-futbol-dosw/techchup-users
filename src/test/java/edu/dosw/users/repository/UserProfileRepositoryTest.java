package edu.dosw.users.repository;

import edu.dosw.users.entity.UserProfileEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserProfileRepositoryTest {

    @Autowired
    private UserProfileRepository repository;

    private UserProfileEntity buildUser(String identification, String email) {
        return UserProfileEntity.builder()
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

        Optional<UserProfileEntity> result = repository.findByIdentification("12345678");

        assertTrue(result.isPresent());
        assertEquals("12345678", result.get().getIdentification());
    }

    @Test
    void findByIdentification_returnsEmpty_whenNotExists() {
        Optional<UserProfileEntity> result = repository.findByIdentification("99999999");

        assertFalse(result.isPresent());
    }

    @Test
    void findByIdentification_returnsCorrectUser_whenMultipleExist() {
        repository.save(buildUser("11111111", "user1@test.com"));
        repository.save(buildUser("22222222", "user2@test.com"));

        Optional<UserProfileEntity> result = repository.findByIdentification("22222222");

        assertTrue(result.isPresent());
        assertEquals("22222222", result.get().getIdentification());
    }
}