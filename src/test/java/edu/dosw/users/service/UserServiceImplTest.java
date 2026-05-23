package edu.dosw.users.service;

import edu.dosw.users.client.IdentityServiceClient;
import edu.dosw.users.client.TeamsServiceClient;
import edu.dosw.users.dto.AccountDto;
import edu.dosw.users.entity.SportProfileEntity;
import edu.dosw.users.entity.UserEntity;
import edu.dosw.users.enums.AuditAction;
import edu.dosw.users.enums.Gender;
import edu.dosw.users.enums.SchoolRelation;
import edu.dosw.users.exception.BusinessException;
import edu.dosw.users.exception.ResourceNotFoundException;
import edu.dosw.users.mapper.UserMapper;
import edu.dosw.users.model.UserModel;
import edu.dosw.users.repository.SportProfileRepository;
import edu.dosw.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private TeamsServiceClient teamsServiceClient;
    @Mock private SportProfileRepository sportProfileRepository;
    @Mock private IAuditService auditService;
    @Mock private IdentityServiceClient identityServiceClient;

    @InjectMocks private UserServiceImpl service;

    // ── getById ──────────────────────────────────────────────────────────────

    @Test
    void getById_found_returnsModel() {
        UserEntity entity = UserEntity.builder().id(1L).build();
        UserModel model = UserModel.builder().id(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(userMapper.toModel(entity)).thenReturn(model);

        assertEquals(1L, service.getById(1L).getId());
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        // No está en local Y Identity devuelve null → 404
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        when(identityServiceClient.getAccountById(99L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> service.getById(99L));
    }

    @Test
    void getById_notInLocal_fallsBackToIdentity_returnsModel() {
        AccountDto account = new AccountDto();
        account.setId(10L);
        account.setName("Ana");
        account.setLastName("Lopez");
        account.setEmail("ana@escuelaing.edu.co");
        account.setStatus("ACTIVE");

        when(userRepository.findById(10L)).thenReturn(Optional.empty());
        when(identityServiceClient.getAccountById(10L)).thenReturn(account);

        UserModel result = service.getById(10L);

        assertEquals(10L, result.getId());
        assertEquals("Ana Lopez", result.getFullName());
        assertEquals("ACTIVE", result.getStatus());
        verify(userRepository, never()).save(any()); // read-through: no persiste
    }

    // ── getByIdentification ───────────────────────────────────────────────────

    @Test
    void getByIdentification_found_returnsModel() {
        UserEntity entity = UserEntity.builder().id(2L).build();
        UserModel model = UserModel.builder().id(2L).build();
        when(userRepository.findByIdentification("12345")).thenReturn(Optional.of(entity));
        when(userMapper.toModel(entity)).thenReturn(model);

        assertEquals(2L, service.getByIdentification("12345").getId());
    }

    @Test
    void getByIdentification_notFound_throwsResourceNotFoundException() {
        when(userRepository.findByIdentification("xxx")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getByIdentification("xxx"));
    }

    // ── getAll ────────────────────────────────────────────────────────────────

    @Test
    void getAll_returnsMappedList() {
        UserEntity e1 = UserEntity.builder().id(1L).build();
        UserEntity e2 = UserEntity.builder().id(2L).build();
        when(userRepository.findAll()).thenReturn(List.of(e1, e2));
        when(userMapper.toModel(e1)).thenReturn(UserModel.builder().id(1L).build());
        when(userMapper.toModel(e2)).thenReturn(UserModel.builder().id(2L).build());

        assertEquals(2, service.getAll().size());
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_success_logsAuditWhenSportProfileExists() {
        Long id = 1L;
        UserEntity entity = UserEntity.builder().id(id).status("ACTIVE").build();
        UserModel incoming = UserModel.builder()
                .fullName("New Name")
                .schoolRelation(SchoolRelation.STUDENT)
                .semester(3)
                .build();
        UserModel result = UserModel.builder().id(id).fullName("New Name").build();
        SportProfileEntity sp = SportProfileEntity.builder().id(10L).build();

        when(userRepository.findById(id)).thenReturn(Optional.of(entity));
        when(userRepository.save(any())).thenReturn(entity);
        when(userMapper.toModel(entity)).thenReturn(result);
        when(sportProfileRepository.findByUserId(id)).thenReturn(Optional.of(sp));

        UserModel updated = service.update(id, incoming);

        assertThat(updated).isNotNull();
        verify(userRepository).save(entity);
        verify(auditService).logSportProfile(10L, AuditAction.UPDATE,
                "Admin updated user with id: " + id);
    }

    @Test
    void update_noSportProfile_skipsAuditLog() {
        Long id = 2L;
        UserEntity entity = UserEntity.builder().id(id).status("ACTIVE").build();
        UserModel incoming = UserModel.builder().fullName("Test").build();

        when(userRepository.findById(id)).thenReturn(Optional.of(entity));
        when(userRepository.save(any())).thenReturn(entity);
        when(userMapper.toModel(entity)).thenReturn(UserModel.builder().id(id).build());
        when(sportProfileRepository.findByUserId(id)).thenReturn(Optional.empty());

        service.update(id, incoming);

        verify(auditService, never()).logSportProfile(any(), any(), any());
    }

    @Test
    void update_throwsNotFoundWhenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, new UserModel()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: 99");
    }

    @Test
    void update_throwsWhenUserInactive() {
        UserEntity entity = UserEntity.builder().id(2L).status("INACTIVE").build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.update(2L, new UserModel()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Cannot update an inactive user.");
    }

    @Test
    void update_throwsWhenSemesterSetForNonStudent() {
        UserEntity entity = UserEntity.builder().id(3L).status("ACTIVE").build();
        UserModel incoming = UserModel.builder()
                .schoolRelation(SchoolRelation.ADMINISTRATIVE)
                .semester(2)
                .build();
        when(userRepository.findById(3L)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> service.update(3L, incoming))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Semester can only be set for students.");
    }

    // ── updateProfile ──────────────────────────────────────────────────────

    @Test
    void updateProfile_updatesAndReturnsModel() {
        UserEntity entity = UserEntity.builder().id(1L).status("ACTIVE").build();
        UserModel updated = UserModel.builder().id(1L).fullName("Nuevo").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(userRepository.save(any())).thenReturn(entity);
        when(userMapper.toModel(entity)).thenReturn(updated);

        UserModel result = service.updateProfile(1L, new UserModel());

        assertEquals("Nuevo", result.getFullName());
    }

    @Test
    void updateProfile_notFound_throwsResourceNotFoundException() {
        // No está en local Y Identity devuelve null → 404
        when(userRepository.findById(55L)).thenReturn(Optional.empty());
        when(identityServiceClient.getAccountById(55L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateProfile(55L, new UserModel()));
    }

    @Test
    void updateProfile_notInLocal_upsertsFromIdentityThenUpdates() {
        AccountDto account = new AccountDto();
        account.setId(20L);
        account.setName("Carlos");
        account.setLastName("Ruiz");
        account.setEmail("carlos@escuelaing.edu.co");
        account.setStatus("ACTIVE");

        UserEntity savedEntity = UserEntity.builder().id(20L).fullName("Carlos Ruiz")
                .email("carlos@escuelaing.edu.co").status("ACTIVE").build();
        UserModel savedModel = UserModel.builder().id(20L).fullName("Carlos Ruiz").build();

        when(userRepository.findById(20L)).thenReturn(Optional.empty());
        when(identityServiceClient.getAccountById(20L)).thenReturn(account);
        when(userRepository.save(any())).thenReturn(savedEntity);
        when(userMapper.toModel(savedEntity)).thenReturn(savedModel);

        UserModel result = service.updateProfile(20L, new UserModel());

        assertEquals(20L, result.getId());
        verify(userRepository).save(any()); // debe persistir el nuevo registro
    }

    // ── deactivate ───────────────────────────────────────────────────────────

    @Test
    void deactivate_setsStatusInactive() {
        UserEntity entity = UserEntity.builder().id(1L).status("ACTIVE").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));

        service.deactivate(1L);

        assertEquals("INACTIVE", entity.getStatus());
        verify(userRepository).save(entity);
    }

    @Test
    void deactivate_notFound_createsStubAndSetsInactive() {
        // El usuario no existe localmente (registrado solo en Identity Service).
        // deactivate() crea un registro mínimo con el ID recibido para poder
        // persistir el estado INACTIVE — esto garantiza que las llamadas de
        // sincronización desde Identity Service nunca fallen con 404.
        when(userRepository.findById(5L)).thenReturn(Optional.empty());

        service.deactivate(5L);

        verify(userRepository).save(argThat(e ->
                "INACTIVE".equals(e.getStatus()) && Long.valueOf(5L).equals(e.getId())));
    }

    // ── inactivate ─────────────────────────────────────────────────────────

    @Test
    void inactivate_activeUserWithoutTeam_setsInactive() {
        UserEntity entity = UserEntity.builder().id(2L).status("ACTIVE").build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(entity));
        when(teamsServiceClient.isPlayerAssignedToTeam(2L)).thenReturn(false);

        service.inactivate(2L);

        assertEquals("INACTIVE", entity.getStatus());
        verify(userRepository).save(entity);
    }

    @Test
    void inactivate_notFound_throwsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.inactivate(99L));
        verify(userRepository, never()).save(any());
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

    // ── reactivate ─────────────────────────────────────────────────────────

    @Test
    void reactivate_inactiveUser_setsActive() {
        UserEntity entity = UserEntity.builder().id(5L).status("INACTIVE").build();
        when(userRepository.findById(5L)).thenReturn(Optional.of(entity));

        service.reactivate(5L);

        assertEquals("ACTIVE", entity.getStatus());
        verify(userRepository).save(entity);
    }

    @Test
    void reactivate_alreadyActive_throwsBusinessException() {
        UserEntity entity = UserEntity.builder().id(6L).status("ACTIVE").build();
        when(userRepository.findById(6L)).thenReturn(Optional.of(entity));

        assertThrows(BusinessException.class, () -> service.reactivate(6L));
    }

    @Test
    void reactivate_notFound_throwsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.reactivate(99L));
    }

    // ── search ───────────────────────────────────────────────────────────────

    @Test
    void search_withAllParams_normalizesAndFilters() {
        UserEntity e1 = UserEntity.builder().id(1L).fullName("juan").status("ACTIVE").build();
        UserModel m1 = UserModel.builder().id(1L).build();
        SportProfileEntity sp = SportProfileEntity.builder().userId(1L).position("FORWARD").build();

        when(userRepository.findAll()).thenReturn(List.of(e1));
        when(userMapper.toModel(e1)).thenReturn(m1);
        when(sportProfileRepository.findByPosition("FORWARD")).thenReturn(List.of(sp));

        List<UserModel> result = service.search(" juan ", "forward", "active", null, null, null, null, null);

        assertEquals(1, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void search_withNullParams_returnsAll() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserModel> result = service.search(null, null, null, null, null, null, null, null);

        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
    }

    @Test
    void search_withPosition_filtersToMatchingUsers() {
        UserEntity e1 = UserEntity.builder().id(1L).status("ACTIVE").build();
        UserEntity e2 = UserEntity.builder().id(2L).status("ACTIVE").build();
        UserModel m1 = UserModel.builder().id(1L).build();
        SportProfileEntity sp = SportProfileEntity.builder().userId(1L).position("GOALKEEPER").build();

        when(userRepository.findAll()).thenReturn(List.of(e1, e2));
        when(userMapper.toModel(e1)).thenReturn(m1);
        when(sportProfileRepository.findByPosition("GOALKEEPER")).thenReturn(List.of(sp));

        List<UserModel> result = service.search(null, "GOALKEEPER", null, null, null, null, null, null);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void search_withOnlyAvailable_filtersToAvailablePlayers() {
        UserEntity e1 = UserEntity.builder().id(1L).status("ACTIVE").build();
        UserEntity e2 = UserEntity.builder().id(2L).status("ACTIVE").build();
        UserModel m1 = UserModel.builder().id(1L).build();
        SportProfileEntity sp = SportProfileEntity.builder().userId(1L).available(true).build();

        when(userRepository.findAll()).thenReturn(List.of(e1, e2));
        when(userMapper.toModel(e1)).thenReturn(m1);
        when(sportProfileRepository.findByAvailable(true)).thenReturn(List.of(sp));

        List<UserModel> result = service.search(null, null, null, null, null, null, null, true);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void search_withGender_filtersLocally() {
        UserEntity e1 = UserEntity.builder().id(1L).gender(Gender.MALE).status("ACTIVE").build();
        UserEntity e2 = UserEntity.builder().id(2L).gender(Gender.FEMALE).status("ACTIVE").build();
        UserModel m1 = UserModel.builder().id(1L).gender(Gender.MALE).build();

        when(userRepository.findAll()).thenReturn(List.of(e1, e2));
        when(userMapper.toModel(e1)).thenReturn(m1);

        List<UserModel> result = service.search(null, null, null, null, "MALE", null, null, null);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void search_withSemester_filtersLocally() {
        UserEntity e1 = UserEntity.builder().id(1L).semester(5).status("ACTIVE").build();
        UserEntity e2 = UserEntity.builder().id(2L).semester(8).status("ACTIVE").build();
        UserModel m1 = UserModel.builder().id(1L).semester(5).build();

        when(userRepository.findAll()).thenReturn(List.of(e1, e2));
        when(userMapper.toModel(e1)).thenReturn(m1);

        List<UserModel> result = service.search(null, null, null, null, null, 5, null, null);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }
}
