package edu.dosw.users.repository;

import edu.dosw.users.entity.SportProfileEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link SportProfileRepository}.
 *
 * <p>Verifies the derived queries used to locate sport profiles by user
 * identifier and position.</p>
 */
@SpringBootTest
@Transactional
class SportProfileRepositoryTest {

    @Autowired
    private SportProfileRepository repository;

    private SportProfileEntity savedProfile(Long userId, String position) {
        return repository.save(SportProfileEntity.builder()
                .userId(userId)
                .position(position)
                .available(true)
                .build());
    }

    @Test
    void findByUserId_returnsSportProfile_whenExists() {
        savedProfile(10L, "MIDFIELDER");

        Optional<SportProfileEntity> result = repository.findByUserId(10L);

        assertTrue(result.isPresent());
        assertEquals(10L, result.get().getUserId());
        assertEquals("MIDFIELDER", result.get().getPosition());
    }

    @Test
    void findByUserId_returnsEmpty_whenUserHasNoSportProfile() {
        Optional<SportProfileEntity> result = repository.findByUserId(999L);

        assertFalse(result.isPresent());
    }

    @Test
    void findByPosition_returnsMatchingProfiles() {
        savedProfile(1L, "FORWARD");
        savedProfile(2L, "FORWARD");
        savedProfile(3L, "DEFENDER");

        List<SportProfileEntity> result = repository.findByPosition("FORWARD");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(sp -> "FORWARD".equals(sp.getPosition())));
    }

    @Test
    void findByPosition_returnsEmpty_whenNoMatch() {
        savedProfile(1L, "GOALKEEPER");

        List<SportProfileEntity> result = repository.findByPosition("FORWARD");

        assertTrue(result.isEmpty());
    }
}
