package edu.dosw.users.service;

import edu.dosw.users.client.TeamsServiceClient;
import edu.dosw.users.entity.SportProfileEntity;
import edu.dosw.users.entity.UserEntity;
import edu.dosw.users.enums.AuditAction;
import edu.dosw.users.exception.BusinessException;
import edu.dosw.users.exception.ResourceNotFoundException;
import edu.dosw.users.mapper.SportProfileMapper;
import edu.dosw.users.model.SportProfileModel;
import edu.dosw.users.repository.SportProfileRepository;
import edu.dosw.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SportProfileServiceImpl}.
 *
 * <p>Uses mocked collaborators to verify sport profile lookup, creation,
 * update restrictions based on team assignment, photo replacement behaviour,
 * audit logging, and availability updates.</p>
 */
@ExtendWith(MockitoExtension.class)
class SportProfileServiceImplTest {

    @Mock private SportProfileRepository sportProfileRepository;
    @Mock private UserRepository userRepository;
    @Mock private SportProfileMapper sportProfileMapper;
    @Mock private IAuditService auditService;
    @Mock private ImageService imageService;
    @Mock private TeamsServiceClient teamsServiceClient;

    @InjectMocks private SportProfileServiceImpl service;

    // ── getById ──────────────────────────────────────────────────────────────

    @Test
    void getById_found_returnsModel() {
        SportProfileEntity entity = SportProfileEntity.builder().id(1L).build();
        SportProfileModel model = SportProfileModel.builder().id(1L).build();
        when(sportProfileRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(sportProfileMapper.toModel(entity)).thenReturn(model);

        assertEquals(1L, service.getById(1L).getId());
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        when(sportProfileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(99L));
    }

    // ── getByUserId ───────────────────────────────────────────────────────────

    @Test
    void getByUserId_found_returnsModel() {
        SportProfileEntity entity = SportProfileEntity.builder().id(2L).build();
        SportProfileModel model = SportProfileModel.builder().id(2L).userId(10L).build();
        when(sportProfileRepository.findByUser_Id(10L)).thenReturn(Optional.of(entity));
        when(sportProfileMapper.toModel(entity)).thenReturn(model);

        assertEquals(10L, service.getByUserId(10L).getUserId());
    }

    @Test
    void getByUserId_notFound_throwsResourceNotFoundException() {
        when(sportProfileRepository.findByUser_Id(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getByUserId(99L));
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    void create_newProfile_savesAndLogsAndReturnsModel() {
        UserEntity user = UserEntity.builder().id(1L).build();
        SportProfileModel input = SportProfileModel.builder().build();
        SportProfileEntity mappedEntity = SportProfileEntity.builder().build();
        SportProfileEntity savedEntity = SportProfileEntity.builder().id(5L).build();
        SportProfileModel savedModel = SportProfileModel.builder().id(5L).userId(1L).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(sportProfileRepository.findByUser_Id(1L)).thenReturn(Optional.empty());
        when(sportProfileMapper.toEntity(input)).thenReturn(mappedEntity);
        when(sportProfileRepository.save(any())).thenReturn(savedEntity);
        when(sportProfileMapper.toModel(savedEntity)).thenReturn(savedModel);

        SportProfileModel result = service.create(1L, input, null);

        assertEquals(5L, result.getId());
        verify(auditService).logSportProfile(eq(5L), eq(AuditAction.CREATE), any(String.class));
        verify(imageService, never()).upload(any(), any());
    }

    @Test
    void create_withPhoto_uploadsPhotoAndSetsPhotoId() {
        MockMultipartFile photo = new MockMultipartFile("photo", "p.jpg",
                "image/jpeg", new byte[]{1, 2, 3});
        UserEntity user = UserEntity.builder().id(1L).build();
        SportProfileModel input = SportProfileModel.builder().build();
        SportProfileEntity mappedEntity = SportProfileEntity.builder().build();
        SportProfileEntity savedEntity = SportProfileEntity.builder().id(6L).build();
        SportProfileModel savedModel = SportProfileModel.builder().id(6L).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(sportProfileRepository.findByUser_Id(1L)).thenReturn(Optional.empty());
        when(sportProfileMapper.toEntity(input)).thenReturn(mappedEntity);
        when(imageService.upload(photo, null)).thenReturn("abc123");
        when(sportProfileRepository.save(any())).thenReturn(savedEntity);
        when(sportProfileMapper.toModel(savedEntity)).thenReturn(savedModel);

        service.create(1L, input, photo);

        assertEquals("abc123", mappedEntity.getPhotoId());
    }

    @Test
    void create_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        SportProfileModel emptyModel = SportProfileModel.builder().build();
        assertThrows(ResourceNotFoundException.class,
                () -> service.create(99L, emptyModel, null));
    }

    @Test
    void create_profileAlreadyExists_throwsBusinessException() {
        UserEntity user = UserEntity.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(sportProfileRepository.findByUser_Id(1L))
                .thenReturn(Optional.of(SportProfileEntity.builder().id(3L).build()));

        SportProfileModel emptyModel = SportProfileModel.builder().build();
        assertThrows(BusinessException.class,
                () -> service.create(1L, emptyModel, null));
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    void update_notInTeam_updatesAndLogsAndReturnsModel() {
        UserEntity user = UserEntity.builder().id(1L).build();
        SportProfileEntity existing = SportProfileEntity.builder().id(3L)
                .user(user).photoId(null).build();
        SportProfileModel updateData = SportProfileModel.builder().build();
        SportProfileEntity updatedEntity = SportProfileEntity.builder().id(3L).build();
        SportProfileModel updatedModel = SportProfileModel.builder().id(3L).build();

        when(sportProfileRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(teamsServiceClient.isPlayerAssignedToTeam(1L)).thenReturn(false);
        when(sportProfileMapper.toEntity(updateData)).thenReturn(updatedEntity);
        when(sportProfileRepository.save(any())).thenReturn(updatedEntity);
        when(sportProfileMapper.toModel(updatedEntity)).thenReturn(updatedModel);

        SportProfileModel result = service.update(3L, updateData, null);

        assertEquals(3L, result.getId());
        verify(auditService).logSportProfile(eq(3L), eq(AuditAction.UPDATE), any(String.class));
    }

    @Test
    void update_playerInTeam_throwsBusinessException() {
        UserEntity user = UserEntity.builder().id(1L).build();
        SportProfileEntity existing = SportProfileEntity.builder().id(3L)
                .user(user).build();
        when(sportProfileRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(teamsServiceClient.isPlayerAssignedToTeam(1L)).thenReturn(true);

        SportProfileModel emptyModel = SportProfileModel.builder().build();
        assertThrows(BusinessException.class,
                () -> service.update(3L, emptyModel, null));
    }

    @Test
    void update_notFound_throwsResourceNotFoundException() {
        when(sportProfileRepository.findById(99L)).thenReturn(Optional.empty());

        SportProfileModel emptyModel = SportProfileModel.builder().build();
        assertThrows(ResourceNotFoundException.class,
                () -> service.update(99L, emptyModel, null));
    }

    @Test
    void update_withNewPhoto_deletesOldAndUploadsNew() {
        UserEntity user = UserEntity.builder().id(1L).build();
        SportProfileEntity existing = SportProfileEntity.builder().id(3L)
                .user(user).photoId("old123").build();
        MockMultipartFile newPhoto = new MockMultipartFile("photo", "new.jpg",
                "image/jpeg", new byte[]{9, 8});
        SportProfileModel updateData = SportProfileModel.builder().build();
        SportProfileEntity updatedEntity = SportProfileEntity.builder().id(3L).build();
        SportProfileModel updatedModel = SportProfileModel.builder().id(3L).build();

        when(sportProfileRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(teamsServiceClient.isPlayerAssignedToTeam(1L)).thenReturn(false);
        when(sportProfileMapper.toEntity(updateData)).thenReturn(updatedEntity);
        when(imageService.upload(newPhoto, null)).thenReturn("new456");
        when(sportProfileRepository.save(any())).thenReturn(updatedEntity);
        when(sportProfileMapper.toModel(updatedEntity)).thenReturn(updatedModel);

        service.update(3L, updateData, newPhoto);

        verify(imageService).delete("old123");
        assertEquals("new456", updatedEntity.getPhotoId());
    }

    // ── updateAvailability ────────────────────────────────────────────────────

    @Test
    void updateAvailability_found_updatesFlag() {
        SportProfileEntity entity = SportProfileEntity.builder().id(1L).available(false).build();
        when(sportProfileRepository.findById(1L)).thenReturn(Optional.of(entity));

        service.updateAvailability(1L, true);

        assertTrue(entity.isAvailable());
        assertNotNull(entity.getUpdatedAt());
        verify(sportProfileRepository).save(entity);
    }

    @Test
    void updateAvailability_notFound_throwsResourceNotFoundException() {
        when(sportProfileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateAvailability(99L, true));
    }
}
