package edu.dosw.users.service;

import edu.dosw.users.entity.UserProfileEntity;
import edu.dosw.users.exception.ResourceNotFoundException;
import edu.dosw.users.mapper.UserProfileMapper;
import edu.dosw.users.model.UserProfileModel;
import edu.dosw.users.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceImplTest {

    @Mock private UserProfileRepository userProfileRepository;
    @Mock private UserProfileMapper userProfileMapper;

    @InjectMocks private UserProfileServiceImpl service;

    // ── getById ──────────────────────────────────────────────────────────────

    @Test
    void getById_found_returnsModel() {
        UserProfileEntity entity = UserProfileEntity.builder().id(1L).build();
        UserProfileModel model = UserProfileModel.builder().id(1L).build();
        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(userProfileMapper.toModel(entity)).thenReturn(model);

        UserProfileModel result = service.getById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        when(userProfileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(99L));
    }

    // ── getByIdentification ──────────────────────────────────────────────────

    @Test
    void getByIdentification_found_returnsModel() {
        UserProfileEntity entity = UserProfileEntity.builder().id(2L).identification("12345").build();
        UserProfileModel model = UserProfileModel.builder().id(2L).build();
        when(userProfileRepository.findByIdentification("12345")).thenReturn(Optional.of(entity));
        when(userProfileMapper.toModel(entity)).thenReturn(model);

        UserProfileModel result = service.getByIdentification("12345");

        assertEquals(2L, result.getId());
    }

    @Test
    void getByIdentification_notFound_throwsResourceNotFoundException() {
        when(userProfileRepository.findByIdentification("xxx")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getByIdentification("xxx"));
    }

    // ── getAll ───────────────────────────────────────────────────────────────

    @Test
    void getAll_returnsMappedList() {
        UserProfileEntity e1 = UserProfileEntity.builder().id(1L).build();
        UserProfileEntity e2 = UserProfileEntity.builder().id(2L).build();
        UserProfileModel m1 = UserProfileModel.builder().id(1L).build();
        UserProfileModel m2 = UserProfileModel.builder().id(2L).build();
        when(userProfileRepository.findAll()).thenReturn(List.of(e1, e2));
        when(userProfileMapper.toModel(e1)).thenReturn(m1);
        when(userProfileMapper.toModel(e2)).thenReturn(m2);

        List<UserProfileModel> result = service.getAll();

        assertEquals(2, result.size());
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    void create_setsStatusAndTimestamps_andReturnsModel() {
        UserProfileModel input = UserProfileModel.builder()
                .fullName("Carlos").email("carlos@eci.edu.co")
                .password("hash").identification("123")
                .build();
        UserProfileEntity entity = UserProfileEntity.builder().id(10L).build();
        UserProfileModel expected = UserProfileModel.builder().id(10L).status("ACTIVE").build();

        when(userProfileMapper.toEntity(any())).thenReturn(entity);
        when(userProfileRepository.save(entity)).thenReturn(entity);
        when(userProfileMapper.toModel(entity)).thenReturn(expected);

        UserProfileModel result = service.create(input);

        assertEquals("ACTIVE", input.getStatus());
        assertNotNull(input.getProfileCreatedAt());
        assertNotNull(input.getUpdatedAt());
        assertEquals(10L, result.getId());
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    void update_existingProfile_updatesAndReturnsModel() {
        UserProfileEntity existing = UserProfileEntity.builder().id(1L).build();
        UserProfileModel updateData = UserProfileModel.builder().fullName("Nuevo").build();
        UserProfileEntity updatedEntity = UserProfileEntity.builder().id(1L).build();
        UserProfileModel updatedModel = UserProfileModel.builder().id(1L).fullName("Nuevo").build();

        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userProfileMapper.toEntity(updateData)).thenReturn(updatedEntity);
        when(userProfileRepository.save(updatedEntity)).thenReturn(updatedEntity);
        when(userProfileMapper.toModel(updatedEntity)).thenReturn(updatedModel);

        UserProfileModel result = service.update(1L, updateData);

        assertEquals(1L, updateData.getId());
        assertNotNull(updateData.getUpdatedAt());
        assertEquals("Nuevo", result.getFullName());
    }

    @Test
    void update_notFound_throwsResourceNotFoundException() {
        when(userProfileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.update(99L, UserProfileModel.builder().build()));
    }

    // ── deactivate ───────────────────────────────────────────────────────────

    @Test
    void deactivate_setsStatusInactiveAndSaves() {
        UserProfileEntity entity = UserProfileEntity.builder().id(1L).status("ACTIVE").build();
        when(userProfileRepository.findById(1L)).thenReturn(Optional.of(entity));

        service.deactivate(1L);

        assertEquals("INACTIVE", entity.getStatus());
        assertNotNull(entity.getUpdatedAt());
        verify(userProfileRepository).save(entity);
    }

    @Test
    void deactivate_notFound_throwsResourceNotFoundException() {
        when(userProfileRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deactivate(5L));
    }
}