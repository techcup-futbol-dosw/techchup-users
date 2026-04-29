package edu.dosw.users.mapper;

import edu.dosw.users.entity.AuditLogEntity;
import edu.dosw.users.entity.InvitationEntity;
import edu.dosw.users.entity.SportProfileEntity;
import edu.dosw.users.enums.AuditAction;
import edu.dosw.users.model.AuditLogModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link AuditLogMapper}.
 *
 * <p>Verifies bidirectional conversion between {@link AuditLogEntity} and
 * {@link AuditLogModel}, including extraction of {@code sportProfile.id} and
 * {@code invitation.id}, handling of null relationships, and the fact that
 * both relationships are ignored in {@code toEntity}.</p>
 */
class AuditLogMapperTest {

    private AuditLogMapper mapper;

    /**
     * Initialises the MapStruct-generated implementation before each test.
     */
    @BeforeEach
    void setUp() {
        mapper = new AuditLogMapperImpl();
    }

    // ── toModel ──────────────────────────────────────────────────────────────

    /**
     * Verifies that {@code toModel} correctly maps all entity fields to the
     * model, extracting {@code sportProfile.id} and {@code invitation.id}
     * into the flat fields of the model.
     */
    @Test
    void toModel_mapsSportProfileIdAndInvitationId() {
        SportProfileEntity sportProfile = SportProfileEntity.builder().id(3L).build();
        InvitationEntity invitation = InvitationEntity.builder().id(7L).build();
        AuditLogEntity entity = AuditLogEntity.builder()
                .id(1L)
                .sportProfile(sportProfile)
                .invitation(invitation)
                .action("CREATE")
                .timestamp(LocalDateTime.of(2024, 5, 1, 10, 0))
                .details("Perfil deportivo creado")
                .build();

        AuditLogModel model = mapper.toModel(entity);

        assertNotNull(model);
        assertEquals(1L, model.getId());
        assertEquals(3L, model.getSportProfileId());
        assertEquals(7L, model.getInvitationId());
        assertEquals(AuditAction.CREATE, model.getAction());
        assertEquals(LocalDateTime.of(2024, 5, 1, 10, 0), model.getTimestamp());
        assertEquals("Perfil deportivo creado", model.getDetails());
    }

    /**
     * Verifies that when {@code sportProfile} is {@code null}, the
     * {@code sportProfileId} field of the resulting model is also {@code null}.
     */
    @Test
    void toModel_nullSportProfile_sportProfileIdIsNull() {
        InvitationEntity invitation = InvitationEntity.builder().id(7L).build();
        AuditLogEntity entity = AuditLogEntity.builder()
                .id(1L)
                .sportProfile(null)
                .invitation(invitation)
                .action("UPDATE")
                .timestamp(LocalDateTime.now())
                .build();

        AuditLogModel model = mapper.toModel(entity);

        assertNull(model.getSportProfileId());
        assertEquals(7L, model.getInvitationId());
        assertEquals(AuditAction.UPDATE, model.getAction());
    }

    /**
     * Verifies that when {@code invitation} is {@code null}, the
     * {@code invitationId} field of the resulting model is also {@code null}.
     */
    @Test
    void toModel_nullInvitation_invitationIdIsNull() {
        SportProfileEntity sportProfile = SportProfileEntity.builder().id(3L).build();
        AuditLogEntity entity = AuditLogEntity.builder()
                .id(1L)
                .sportProfile(sportProfile)
                .invitation(null)
                .action("DEACTIVATE")
                .timestamp(LocalDateTime.now())
                .build();

        AuditLogModel model = mapper.toModel(entity);

        assertEquals(3L, model.getSportProfileId());
        assertNull(model.getInvitationId());
        assertEquals(AuditAction.DEACTIVATE, model.getAction());
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
     * entity and that both relationships ({@code sportProfile} and
     * {@code invitation}) are left as {@code null}.
     */
    @Test
    void toEntity_mapsFields_andIgnoresBothRelations() {
        AuditLogModel model = AuditLogModel.builder()
                .id(1L)
                .sportProfileId(3L)
                .invitationId(7L)
                .action(AuditAction.CREATE)
                .timestamp(LocalDateTime.of(2024, 5, 1, 10, 0))
                .details("Perfil deportivo creado")
                .build();

        AuditLogEntity entity = mapper.toEntity(model);

        assertNotNull(entity);
        assertEquals(1L, entity.getId());
        assertEquals("CREATE", entity.getAction());
        assertEquals("Perfil deportivo creado", entity.getDetails());
        assertNull(entity.getSportProfile());
        assertNull(entity.getInvitation());
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