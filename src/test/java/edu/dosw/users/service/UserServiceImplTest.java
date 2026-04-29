package edu.dosw.users.service;

import edu.dosw.users.entity.UserEntity;
import edu.dosw.users.exception.ResourceNotFoundException;
import edu.dosw.users.mapper.UserMapper;
import edu.dosw.users.model.UserModel;
import edu.dosw.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;

    @InjectMocks private UserServiceImpl service;

    // ── getById ──────────────────────────────────────────────────────────────

    @Test
    void getById_found_returnsModel() {
        UserEntity entity = UserEntity.builder().id(1L).build();
        UserModel model = UserModel.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(userMapper.toModel(entity)).thenReturn(model);

        UserModel result = service.getById(1L);

        assertEquals(1L, result.getId());
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getById(99L));
    }

    // ── getByIdentification ──────────────────────────────────────────────────

    @Test
    void getByIdentification_found_returnsModel() {
        UserEntity entity = UserEntity.builder().id(2L).identification("12345").build();
        UserModel model = UserModel.builder().id(2L).build();
        when(userRepository.findByIdentification("12345")).thenReturn(Optional.of(entity));
        when(userMapper.toModel(entity)).thenReturn(model);

        UserModel result = service.getByIdentification("12345");

        assertEquals(2L, result.getId());
    }

    @Test
    void getByIdentification_notFound_throwsResourceNotFoundException() {
        when(userRepository.findByIdentification("xxx")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getByIdentification("xxx"));
    }

    // ── getAll ───────────────────────────────────────────────────────────────

    @Test
    void getAll_returnsMappedList() {
        UserEntity e1 = UserEntity.builder().id(1L).build();
        UserEntity e2 = UserEntity.builder().id(2L).build();
        UserModel m1 = UserModel.builder().id(1L).build();
        UserModel m2 = UserModel.builder().id(2L).build();
        when(userRepository.findAll()).thenReturn(List.of(e1, e2));
        when(userMapper.toModel(e1)).thenReturn(m1);
        when(userMapper.toModel(e2)).thenReturn(m2);

        List<UserModel> result = service.getAll();

        assertEquals(2, result.size());
    }

    // ── create ───────────────────────────────────────────────────────────────

    @Test
    void create_setsStatusAndTimestamps_andReturnsModel() {
        UserModel input = UserModel.builder()
                .fullName("Carlos").email("carlos@eci.edu.co")
                .password("hash").identification("123")
                .build();
        UserEntity entity = UserEntity.builder().id(10L).build();
        UserModel expected = UserModel.builder().id(10L).status("ACTIVE").build();

        when(userMapper.toEntity(any())).thenReturn(entity);
        when(userRepository.save(entity)).thenReturn(entity);
        when(userMapper.toModel(entity)).thenReturn(expected);

        UserModel result = service.create(input);

        assertEquals("ACTIVE", input.getStatus());
        assertNotNull(input.getProfileCreatedAt());
        assertNotNull(input.getUpdatedAt());
        assertEquals(10L, result.getId());
    }

    // ── update ───────────────────────────────────────────────────────────────

    @Test
    void update_existingProfile_updatesAndReturnsModel() {
        UserEntity existing = UserEntity.builder().id(1L).build();
        UserModel updateData = UserModel.builder().fullName("Nuevo").build();
        UserEntity updatedEntity = UserEntity.builder().id(1L).build();
        UserModel updatedModel = UserModel.builder().id(1L).fullName("Nuevo").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userMapper.toEntity(updateData)).thenReturn(updatedEntity);
        when(userRepository.save(updatedEntity)).thenReturn(updatedEntity);
        when(userMapper.toModel(updatedEntity)).thenReturn(updatedModel);

        UserModel result = service.update(1L, updateData);

        assertEquals(1L, updateData.getId());
        assertNotNull(updateData.getUpdatedAt());
        assertEquals("Nuevo", result.getFullName());
    }

    @Test
    void update_notFound_throwsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        UserModel emptyModel = UserModel.builder().build();

        assertThrows(ResourceNotFoundException.class,
                () -> service.update(99L, emptyModel));
    }

    // ── deactivate ───────────────────────────────────────────────────────────

    @Test
    void deactivate_setsStatusInactiveAndSaves() {
        UserEntity entity = UserEntity.builder().id(1L).status("ACTIVE").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));

        service.deactivate(1L);

        assertEquals("INACTIVE", entity.getStatus());
        assertNotNull(entity.getUpdatedAt());
        verify(userRepository).save(entity);
    }

    @Test
    void deactivate_notFound_throwsResourceNotFoundException() {
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deactivate(5L));
    }
}