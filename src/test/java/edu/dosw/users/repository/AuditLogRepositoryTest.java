package edu.dosw.users.repository;

import edu.dosw.users.entity.AuditLogEntity;
import edu.dosw.users.entity.InvitationEntity;
import edu.dosw.users.entity.SportProfileEntity;
import edu.dosw.users.entity.UserProfileEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class AuditLogRepositoryTest {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private SportProfileRepository sportProfileRepository;

    @Autowired
    private InvitationRepository invitationRepository;

    @Autowired
    private AuditLogRepository repository;

    private SportProfileEntity sportProfile;
    private InvitationEntity invitation;

    @BeforeEach
    void setUp() {
        UserProfileEntity user = userProfileRepository.save(UserProfileEntity.builder()
                .fullName("Test User")
                .email("user@test.com")
                .password("hashed")
                .identification("12345678")
                .status("ACTIVE")
                .build());

        sportProfile = sportProfileRepository.save(SportProfileEntity.builder()
                .userProfile(user)
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