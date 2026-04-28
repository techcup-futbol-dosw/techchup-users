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

class AuditLogMapperTest {

    private AuditLogMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AuditLogMapperImpl();
    }

    // ── toModel ──────────────────────────────────────────────────────────────

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

    @Test
    void toModel_nullEntity_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    // ── toEntity ─────────────────────────────────────────────────────────────

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

    @Test
    void toEntity_nullModel_returnsNull() {
        assertNull(mapper.toEntity(null));
    }
}