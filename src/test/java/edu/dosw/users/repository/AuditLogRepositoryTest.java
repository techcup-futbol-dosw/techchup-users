package edu.dosw.users.repository;

import edu.dosw.users.entity.AuditLogEntity;
import edu.dosw.users.entity.InvitationEntity;
import edu.dosw.users.entity.SportProfileEntity;
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
 * Integration tests for {@link AuditLogRepository}.
 *
 * <p>Runs against the Spring test context and transactional database state to
 * verify the derived queries that retrieve audit logs by sport profile and by
 * invitation.</p>
 */
@SpringBootTest
@Transactional
class AuditLogRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SportProfileRepository sportProfileRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private AuditLogRepository repository;

    private SportProfileEntity sportProfile;
    private InvitationEntity invitation;

    /**
     * Creates the user, sport profile, and invitation records required by the
     * repository query tests.
     */
    @BeforeEach
    void setUp() {
        UserEntity user = userRepository.save(UserEntity.builder()
                .fullName("Test User")
                .email("user@test.com")
                .password("hashed")
                .identification("12345678")
                .status("ACTIVE")
                .build());

        sportProfile = sportProfileRepository.save(SportProfileEntity.builder()
                .user(user)
                .position("FORWARD")
                .available(true)
                .build());

        invitation = invitationRepository.save(InvitationEntity.builder()
                .player(user)
                .teamId(1L)
                .status("PENDING")
                .sentAt(LocalDateTime.now())
                .build());
    }

    /**
     * Persists an audit log entry linked to either a sport profile or an
     * invitation.
     *
     * @param sp sport profile reference to associate, or {@code null}
     * @param inv invitation reference to associate, or {@code null}
     * @param action action value to store in the audit entry
     * @return saved audit log entity
     */
    private AuditLogEntity savedLog(SportProfileEntity sp, InvitationEntity inv, String action) {
        return repository.save(AuditLogEntity.builder()
                .sportProfile(sp)
                .invitation(inv)
                .action(action)
                .timestamp(LocalDateTime.now())
                .details("Acción: " + action)
                .build());
    }

    @Test
    void findBySportProfile_Id_returnsLogsForSportProfile() {
        savedLog(sportProfile, null, "CREATE");
        savedLog(sportProfile, null, "UPDATE");

        List<AuditLogEntity> result = repository.findBySportProfile_Id(sportProfile.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(l -> l.getSportProfile().getId().equals(sportProfile.getId())));
    }

    @Test
    void findBySportProfile_Id_returnsEmpty_whenNoLogsForSportProfile() {
        savedLog(null, invitation, "CREATE");

        List<AuditLogEntity> result = repository.findBySportProfile_Id(sportProfile.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void findByInvitation_Id_returnsLogsForInvitation() {
        savedLog(null, invitation, "CREATE");
        savedLog(null, invitation, "UPDATE");

        List<AuditLogEntity> result = repository.findByInvitation_Id(invitation.getId());

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(l -> l.getInvitation().getId().equals(invitation.getId())));
    }

    @Test
    void findByInvitation_Id_returnsEmpty_whenNoLogsForInvitation() {
        savedLog(sportProfile, null, "CREATE");

        List<AuditLogEntity> result = repository.findByInvitation_Id(invitation.getId());

        assertTrue(result.isEmpty());
    }
}
