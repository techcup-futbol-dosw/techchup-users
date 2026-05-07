package edu.dosw.users.service;

import edu.dosw.users.entity.SportProfileEntity;
import edu.dosw.users.entity.UserEntity;
import edu.dosw.users.enums.SchoolRelation;
import edu.dosw.users.exception.BusinessException;
import edu.dosw.users.exception.ResourceNotFoundException;
import edu.dosw.users.mapper.UserMapper;
import edu.dosw.users.model.UserModel;
import edu.dosw.users.repository.UserRepository;
import edu.dosw.users.client.TeamsServiceClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Consolidated unit tests for {@link UserServiceImpl} covering update
 * behaviour, validation rules and interactions with the audit service.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private IAuditService auditService;
    @Mock private TeamsServiceClient teamsServiceClient;

    @InjectMocks private UserServiceImpl service;

    @Test
    void update_success_logsAuditWhenSportProfileExists() {
        Long id = 1L;
        UserEntity existing = UserEntity.builder()
                .id(id)
                .status("ACTIVE")
                .fullName("Old Name")
                .sportProfile(SportProfileEntity.builder().id(10L).build())
                .build();

        UserModel incoming = UserModel.builder()
                .fullName("New Name")
                .schoolRelation(SchoolRelation.STUDENT)
                .academicProgram("Engineering")
                .semester(3)
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toModel(any(UserEntity.class))).thenReturn(incoming);

        var result = service.update(id, incoming);

        assertThat(result).isNotNull();
        verify(userRepository).save(any(UserEntity.class));
        verify(auditService).logSportProfile(10L, edu.dosw.users.enums.AuditAction.UPDATE,
                "Admin updated user with id: " + id);
    }

    @Test
    void update_throwsNotFoundWhenMissing() {
        Long id = 99L;
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(id, new UserModel()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: " + id);
    }

    @Test
    void update_throwsWhenUserInactive() {
        Long id = 2L;
        UserEntity existing = UserEntity.builder()
                .id(id)
                .status("INACTIVE")
                .build();
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.update(id, new UserModel()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Cannot update an inactive user.");
    }

    @Test
    void update_throwsWhenSemesterSetForNonStudent() {
        Long id = 3L;
        UserEntity existing = UserEntity.builder()
                .id(id)
                .status("ACTIVE")
                .build();
        UserModel incoming = UserModel.builder()
                .schoolRelation(SchoolRelation.ADMINISTRATIVE)
                .semester(2)
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.update(id, incoming))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Semester can only be set for students.");
    }

    // ---- existing service tests (compatibility) ----

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

    @Test
    void update_existingProfile_updatesAndReturnsModel() {
        UserEntity existing = UserEntity.builder().id(1L).status("ACTIVE").build();
        UserModel updateData = UserModel.builder().fullName("Nuevo").build();
        UserModel updatedModel = UserModel.builder().id(1L).fullName("Nuevo").updatedAt(java.time.LocalDateTime.now()).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userMapper.toModel(any(UserEntity.class))).thenReturn(updatedModel);

        UserModel result = service.update(1L, updateData);

        assertEquals(1L, result.getId());
        assertNotNull(result.getUpdatedAt());
        assertEquals("Nuevo", result.getFullName());
    }

    @Test
    void update_notFound_throwsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        UserModel emptyModel = UserModel.builder().build();

        assertThrows(ResourceNotFoundException.class,
                () -> service.update(99L, emptyModel));
    }

        // ── updateProfile ──────────────────────────────────────────────────────

        @Test
        void updateProfile_updatesAllowedFieldsAndPreservesCredentials() {
        UserEntity existing = UserEntity.builder()
            .id(1L)
            .fullName("Anterior")
            .email("old@eci.edu.co")
            .password("secret")
            .identification("111")
            .build();
        UserEntity updateData = UserEntity.builder()
            .fullName("Nuevo")
            .identification("222")
            .birthDate(java.time.LocalDate.of(2000, 1, 1))
            .gender("MALE")
            .schoolRelation("STUDENT")
            .academicProgram("Ingenieria")
            .semester(4)
            .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userMapper.toEntity(any())).thenReturn(updateData);
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toModel(any(UserEntity.class)))
            .thenReturn(UserModel.builder().id(1L).fullName("Nuevo").build());

        UserModel result = service.updateProfile(1L, UserModel.builder().build());

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        UserEntity saved = captor.getValue();
        assertEquals("Nuevo", saved.getFullName());
        assertEquals("222", saved.getIdentification());
        assertEquals("MALE", saved.getGender());
        assertEquals("STUDENT", saved.getSchoolRelation());
        assertEquals("Ingenieria", saved.getAcademicProgram());
        assertEquals(4, saved.getSemester());
        assertEquals("old@eci.edu.co", saved.getEmail());
        assertEquals("secret", saved.getPassword());
        assertNotNull(saved.getUpdatedAt());
        assertEquals("Nuevo", result.getFullName());
        }

        @Test
        void updateProfile_notFound_throwsResourceNotFoundException() {
        when(userRepository.findById(55L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> service.updateProfile(55L, UserModel.builder().build()));
        }

    // ── search ───────────────────────────────────────────────────────────────

    @Test
    void search_withAllParams_normalizesAndCallsRepository() {
        UserEntity e1 = UserEntity.builder().id(1L).build();
        UserModel m1 = UserModel.builder().id(1L).build();
        when(userRepository.searchPlayers("juan", "ACTIVE", "FORWARD")).thenReturn(List.of(e1));
        when(userMapper.toModel(e1)).thenReturn(m1);

        List<UserModel> result = service.search(" juan ", "forward", "active");

        assertEquals(1, result.size());
        verify(userRepository).searchPlayers("juan", "ACTIVE", "FORWARD");
    }

    @Test
    void search_withNullParams_passesNullsToRepository() {
        when(userRepository.searchPlayers(null, null, null)).thenReturn(List.of());

        List<UserModel> result = service.search(null, null, null);

        assertTrue(result.isEmpty());
        verify(userRepository).searchPlayers(null, null, null);
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

    // ── inactivate ─────────────────────────────────────────────────────────

    @Test
    void inactivate_activeUserWithoutTeam_setsInactive() {
        UserEntity entity = UserEntity.builder().id(2L).status("ACTIVE").build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(entity));
        when(teamsServiceClient.isPlayerAssignedToTeam(2L)).thenReturn(false);

        service.inactivate(2L);

        assertEquals("INACTIVE", entity.getStatus());
        assertNotNull(entity.getUpdatedAt());
        verify(userRepository).save(entity);
    }

    @Test
    void inactivate_alreadyInactive_throwsBusinessException() {
        UserEntity entity = UserEntity.builder().id(3L).status("INACTIVE").build();
        when(userRepository.findById(3L)).thenReturn(Optional.of(entity));

        assertThrows(BusinessException.class, () -> service.inactivate(3L));
        verify(userRepository, never()).save(any());
    }

    @Test
    void inactivate_userInActiveTeam_throwsBusinessException() {
        UserEntity entity = UserEntity.builder().id(4L).status("ACTIVE").build();
        when(userRepository.findById(4L)).thenReturn(Optional.of(entity));
        when(teamsServiceClient.isPlayerAssignedToTeam(4L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.inactivate(4L));
        verify(userRepository, never()).save(any());
    }
}
