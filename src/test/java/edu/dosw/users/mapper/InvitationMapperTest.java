package edu.dosw.users.mapper;

import edu.dosw.users.dto.InvitationResponse;
import edu.dosw.users.entity.InvitationEntity;
import edu.dosw.users.enums.InvitationStatus;
import edu.dosw.users.model.InvitationModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link InvitationMapper}.
 *
 * <p>Verifies bidirectional conversion between {@link InvitationEntity} and
 * {@link InvitationModel}, including the mapping of the entity's {@code userId}
 * field to the model's {@code playerId}, and handling of null values.</p>
 */
class InvitationMapperTest {

    private InvitationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new InvitationMapperImpl();
    }

    // ── toModel ──────────────────────────────────────────────────────────────

    @Test
    void toModel_mapsUserIdToPlayerIdAndAllFields() {
        InvitationEntity entity = InvitationEntity.builder()
                .id(1L)
                .userId(10L)
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
    void toModel_nullUserId_playerIdIsNull() {
        InvitationEntity entity = InvitationEntity.builder()
                .id(1L)
                .userId(null)
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
    void toEntity_mapsPlayerIdToUserId() {
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
        assertEquals(10L, entity.getUserId());
        assertEquals(5L, entity.getTeamId());
        assertEquals("REJECTED", entity.getStatus());
    }

    @Test
    void toEntity_nullModel_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    // ── toResponse ───────────────────────────────────────────────────────────

    @Test
    void toResponse_mapsAllFields() {
        InvitationModel model = InvitationModel.builder()
                .id(7L)
                .playerId(10L)
                .teamId(3L)
                .status(InvitationStatus.ACCEPTED)
                .sentAt(LocalDateTime.of(2024, 5, 1, 9, 0))
                .respondedAt(LocalDateTime.of(2024, 5, 2, 11, 0))
                .build();

        InvitationResponse response = mapper.toResponse(model);

        assertNotNull(response);
        assertEquals(7L, response.getId());
        assertEquals(10L, response.getPlayerId());
        assertEquals(3L, response.getTeamId());
        assertEquals(InvitationStatus.ACCEPTED, response.getStatus());
        assertEquals(LocalDateTime.of(2024, 5, 1, 9, 0), response.getSentAt());
        assertEquals(LocalDateTime.of(2024, 5, 2, 11, 0), response.getRespondedAt());
    }

    @Test
    void toResponse_nullModel_returnsNull() {
        assertNull(mapper.toResponse(null));
    }
}
