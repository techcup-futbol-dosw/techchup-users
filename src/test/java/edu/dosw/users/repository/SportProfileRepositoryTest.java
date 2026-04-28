package edu.dosw.users.repository;

import edu.dosw.users.entity.SportProfileEntity;
import edu.dosw.users.entity.UserProfileEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class SportProfileRepositoryTest {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private SportProfileRepository repository;

    private UserProfileEntity savedUser(String identification, String email) {
        return userProfileRepository.save(UserProfileEntity.builder()
                .fullName("Test User")
                .email(email)
                .password("hashed")
                .identification(identification)
                .status("ACTIVE")
                .build());
    }

    private SportProfileEntity savedSportProfile(UserProfileEntity user) {
        return repository.save(SportProfileEntity.builder()
                .userProfile(user)
                .position("MIDFIELDER")
                .available(true)
                .build());
    }

    @Test
    void findByUserProfile_Id_returnsSportProfile_whenExists() {
        UserProfileEntity user = savedUser("12345678", "user@test.com");
        savedSportProfile(user);

        Optional<SportProfileEntity> result = repository.findByUserProfile_Id(user.getId());

        assertTrue(result.isPresent());
        assertEquals(user.getId(), result.get().getUserProfile().getId());
        assertEquals("MIDFIELDER", result.get().getPosition());
    }

    @Test
    void findByUserProfile_Id_returnsEmpty_whenUserHasNoSportProfile() {
        UserProfileEntity user = savedUser("12345678", "user@test.com");

        Optional<SportProfileEntity> result = repository.findByUserProfile_Id(user.getId());

        assertFalse(result.isPresent());
    }

    @Test
    void findByUserProfile_Id_returnsEmpty_whenUserDoesNotExist() {
        Optional<SportProfileEntity> result = repository.findByUserProfile_Id(999L);

        assertFalse(result.isPresent());
    }
}