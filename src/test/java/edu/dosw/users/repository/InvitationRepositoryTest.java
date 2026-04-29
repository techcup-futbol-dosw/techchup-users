package edu.dosw.users.repository;

import edu.dosw.users.entity.InvitationEntity;
import edu.dosw.users.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
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
 * <p>Verifies Spring Data derived queries for retrieving invitations by player
 * and by player/status using transactional database state.</p>
 */
@SpringBootTest
@Transactional
class InvitationRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InvitationRepository repository;

    private UserEntity player;

    /**
     * Creates the player used by the repository query tests.
     */
    @BeforeEach
    void setUp() {
        player = userRepository.save(UserEntity.builder()
                .fullName("Test Player")
                .email("player@test.com")
                .password("hashed")
                .identification("12345678")
                .status("ACTIVE")
                .build());
    }

    /**
     * Persists an invitation for the supplied player, team, and status.
     *
     * @param p player receiving the invitation
     * @param teamId team identifier associated with the invitation
     * @param status invitation status to persist
     * @return saved invitation entity
     */
    private InvitationEntity savedInvitation(UserEntity p, Long teamId, String status) {
        return repository.save(InvitationEntity.builder()
                .player(p)
                .teamId(teamId)
                .status(status)
                .sentAt(LocalDateTime.now())
                .build());
    }

    @Test
    void findByPlayer_Id_returnsAllInvitationsForPlayer() {
        savedInvitation(player, 1L, "PENDING");
        savedInvitation(player, 2L, "ACCEPTED");

        List<InvitationEntity> result = repository.findByPlayer_Id(player.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(i -> i.getPlayer().getId().equals(player.getId())));
    }

    @Test
    void findByPlayer_Id_returnsEmpty_whenPlayerHasNoInvitations() {
        List<InvitationEntity> result = repository.findByPlayer_Id(player.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void findByPlayer_IdAndStatus_returnsOnlyMatchingStatus() {
        savedInvitation(player, 1L, "PENDING");
        savedInvitation(player, 2L, "ACCEPTED");
        savedInvitation(player, 3L, "PENDING");

        List<InvitationEntity> result = repository.findByPlayer_IdAndStatus(player.getId(), "PENDING");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(i -> "PENDING".equals(i.getStatus())));
    }

    @Test
    void findByPlayer_IdAndStatus_returnsEmpty_whenNoMatchingStatus() {
        savedInvitation(player, 1L, "PENDING");

        List<InvitationEntity> result = repository.findByPlayer_IdAndStatus(player.getId(), "ACCEPTED");

        assertTrue(result.isEmpty());
    }
}
