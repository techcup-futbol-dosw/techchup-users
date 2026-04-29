package edu.dosw.users.service;

import edu.dosw.users.entity.AuditLogEntity;
import edu.dosw.users.enums.AuditAction;
import edu.dosw.users.mapper.AuditLogMapper;
import edu.dosw.users.model.AuditLogModel;
import edu.dosw.users.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AuditServiceImpl}.
 *
 * <p>Uses Mockito to verify that audit events are mapped, enriched with the
 * expected id-only relationship references, saved through the repository, and
 * converted back to models for query operations.</p>
 */
@ExtendWith(MockitoExtension.class)
class AuditServiceImplTest {

    @Mock private AuditLogRepository auditLogRepository;
    @Mock private AuditLogMapper auditLogMapper;

    @InjectMocks private AuditServiceImpl auditService;

    @Test
    void logSportProfile_savesEntityWithSportProfileReference() {
        AuditLogEntity mappedEntity = AuditLogEntity.builder()
                .action("CREATE")
                .timestamp(LocalDateTime.now())
                .build();
        when(auditLogMapper.toEntity(any())).thenReturn(mappedEntity);
        when(auditLogRepository.save(any())).thenReturn(mappedEntity);

        auditService.logSportProfile(5L, AuditAction.CREATE, "created");

        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
        verify(auditLogRepository).save(captor.capture());
        assertNotNull(captor.getValue().getSportProfile());
        assertEquals(5L, captor.getValue().getSportProfile().getId());
        assertNull(captor.getValue().getInvitation());
    }

    @Test
    void logInvitation_savesEntityWithInvitationReference() {
        AuditLogEntity mappedEntity = AuditLogEntity.builder()
                .action("UPDATE")
                .timestamp(LocalDateTime.now())
                .build();
        when(auditLogMapper.toEntity(any())).thenReturn(mappedEntity);
        when(auditLogRepository.save(any())).thenReturn(mappedEntity);

        auditService.logInvitation(7L, AuditAction.UPDATE, "updated");

        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
        verify(auditLogRepository).save(captor.capture());
        assertNotNull(captor.getValue().getInvitation());
        assertEquals(7L, captor.getValue().getInvitation().getId());
        assertNull(captor.getValue().getSportProfile());
    }

    @Test
    void getLogsForSportProfile_returnsMappedModels() {
        AuditLogEntity entity = AuditLogEntity.builder().id(1L).build();
        AuditLogModel model = AuditLogModel.builder().id(1L).build();
        when(auditLogRepository.findBySportProfile_Id(3L)).thenReturn(List.of(entity));
        when(auditLogMapper.toModel(entity)).thenReturn(model);

        List<AuditLogModel> result = auditService.getLogsForSportProfile(3L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void getLogsForInvitation_returnsMappedModels() {
        AuditLogEntity entity = AuditLogEntity.builder().id(2L).build();
        AuditLogModel model = AuditLogModel.builder().id(2L).build();
        when(auditLogRepository.findByInvitation_Id(9L)).thenReturn(List.of(entity));
        when(auditLogMapper.toModel(entity)).thenReturn(model);

        List<AuditLogModel> result = auditService.getLogsForInvitation(9L);

        assertEquals(1, result.size());
        assertEquals(2L, result.get(0).getId());
    }

    @Test
    void getLogsForSportProfile_emptyList_returnsEmpty() {
        when(auditLogRepository.findBySportProfile_Id(99L)).thenReturn(List.of());

        List<AuditLogModel> result = auditService.getLogsForSportProfile(99L);

        assertTrue(result.isEmpty());
        verify(auditLogMapper, never()).toModel(any());
    }
}
