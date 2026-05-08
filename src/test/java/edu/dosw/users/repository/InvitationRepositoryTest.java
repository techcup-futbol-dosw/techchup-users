package edu.dosw.users.repository;

import edu.dosw.users.entity.InvitationEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link InvitationRepository}.
 *
 * <p>Verifies Spring Data derived queries for retrieving invitations by user
 * and by user/status.</p>
 */
@SpringBootTest
@Transactional
class InvitationRepositoryTest {

    @Autowired
    private InvitationRepository repository;

    private InvitationEntity savedInvitation(Long userId, Long teamId, String status) {
        return repository.save(InvitationEntity.builder()
                .userId(userId)
                .teamId(teamId)
                .status(status)
                .sentAt(LocalDateTime.now())
                .build());
    }

    @Test
    void findByUserId_returnsAllInvitationsForUser() {
        savedInvitation(10L, 1L, "PENDING");
        savedInvitation(10L, 2L, "ACCEPTED");

        List<InvitationEntity> result = repository.findByUserId(10L);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(i -> i.getUserId().equals(10L)));
    }

    @Test
    void findByUserId_returnsEmpty_whenUserHasNoInvitations() {
        List<InvitationEntity> result = repository.findByUserId(999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUserIdAndStatus_returnsOnlyMatchingStatus() {
        savedInvitation(10L, 1L, "PENDING");
        savedInvitation(10L, 2L, "ACCEPTED");
        savedInvitation(10L, 3L, "PENDING");

        List<InvitationEntity> result = repository.findByUserIdAndStatus(10L, "PENDING");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(i -> "PENDING".equals(i.getStatus())));
    }

    @Test
    void findByUserIdAndStatus_returnsEmpty_whenNoMatchingStatus() {
        savedInvitation(10L, 1L, "PENDING");

        List<InvitationEntity> result = repository.findByUserIdAndStatus(10L, "ACCEPTED");

        assertTrue(result.isEmpty());
    }
}
