package edu.dosw.users.mapper;

import edu.dosw.users.entity.InvitationEntity;
import edu.dosw.users.entity.UserProfileEntity;
import edu.dosw.users.enums.InvitationStatus;
import edu.dosw.users.model.InvitationModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class InvitationMapperTest {

    private InvitationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new InvitationMapperImpl();
    }

    // ── toModel ──────────────────────────────────────────────────────────────

    @Test
    void toModel_mapsPlayerIdAndAllFields() {
        UserProfileEntity player = UserProfileEntity.builder().id(10L).build();
        InvitationEntity entity = InvitationEntity.builder()
                .id(1L)
                .player(player)
                .teamId(5L)
                .status("PENDING")
                .sentAt(LocalDateTime.of(2024, 4, 1, 12, 0))
                .respondedAt(null)
                .build();

        InvitationModel model = mapper.toModel(entity);

        assertNotNull(model);
        assertEquals(1L, model.getId());
        assertEquals(10L, model.getPlayerId());
        assertEquals(5L, model.getTeamId());
        assertEquals(InvitationStatus.PENDING, model.getStatus());
        assertEquals(LocalDateTime.of(2024, 4, 1, 12, 0), model.getSentAt());
        assertNull(model.getRespondedAt());
    }

    @Test
    void toModel_nullPlayer_playerIdIsNull() {
        InvitationEntity entity = InvitationEntity.builder()
                .id(1L)
                .player(null)
                .teamId(5L)
                .status("ACCEPTED")
                .build();

        InvitationModel model = mapper.toModel(entity);

        assertNull(model.getPlayerId());
        assertEquals(InvitationStatus.ACCEPTED, model.getStatus());
    }

    @Test
    void toModel_nullEntity_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    // ── toEntity ─────────────────────────────────────────────────────────────

    @Test
    void toEntity_mapsFields_andIgnoresPlayer() {
        InvitationModel model = InvitationModel.builder()
                .id(1L)
                .playerId(10L)
                .teamId(5L)
                .status(InvitationStatus.REJECTED)
                .sentAt(LocalDateTime.of(2024, 4, 1, 12, 0))
                .respondedAt(LocalDateTime.of(2024, 4, 2, 8, 0))
                .build();

        InvitationEntity entity = mapper.toEntity(model);

        assertNotNull(entity);
        assertEquals(1L, entity.getId());
        assertEquals(5L, entity.getTeamId());
        assertEquals("REJECTED", entity.getStatus());
        assertNull(entity.getPlayer());
    }

    @Test
    void toEntity_nullModel_returnsNull() {
        assertNull(mapper.toEntity(null));
    }
}