package edu.dosw.users.mapper;

import edu.dosw.users.entity.InvitationEntity;
import edu.dosw.users.entity.UserProfileEntity;
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
 * {@link InvitationModel}, including extraction of {@code player.id} into
 * {@code playerId}, handling of a null player, and the fact that the
 * {@code player} relationship is ignored in {@code toEntity}.</p>
 */
class InvitationMapperTest {

    private InvitationMapper mapper;

    /**
     * Initialises the MapStruct-generated implementation before each test.
     */
    @BeforeEach
    void setUp() {
        mapper = new InvitationMapperImpl();
    }

    // ── toModel ──────────────────────────────────────────────────────────────

    /**
     * Verifies that {@code toModel} correctly maps all entity fields to the
     * model, extracting {@code player.id} into {@code playerId}.
     */
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

    /**
     * Verifies that when {@code player} is {@code null}, the {@code playerId}
     * field of the resulting model is also {@code null}.
     */
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

    /**
     * Verifies that {@code toModel} returns {@code null} when given a
     * {@code null} entity.
     */
    @Test
    void toModel_nullEntity_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    // ── toEntity ─────────────────────────────────────────────────────────────

    /**
     * Verifies that {@code toEntity} correctly maps all model fields to the
     * entity and that the {@code player} relationship is left as {@code null}.
     */
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

    /**
     * Verifies that {@code toEntity} returns {@code null} when given a
     * {@code null} model.
     */
    @Test
    void toEntity_nullModel_returnsNull() {
        assertNull(mapper.toEntity(null));
    }
}