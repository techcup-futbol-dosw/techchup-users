package edu.dosw.users.service;

import edu.dosw.users.client.IdentityServiceClient;
import edu.dosw.users.client.TeamsServiceClient;
import edu.dosw.users.entity.SportProfileEntity;
import edu.dosw.users.enums.AuditAction;
import edu.dosw.users.enums.SchoolRelation;
import edu.dosw.users.exception.BusinessException;
import edu.dosw.users.exception.ResourceNotFoundException;
import edu.dosw.users.model.UserModel;
import edu.dosw.users.repository.SportProfileRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserServiceImpl}.
 *
 * <p>Verifies delegation to {@link IdentityServiceClient}, business-rule
 * enforcement (active check, semester constraint, team assignment) and
 * audit logging on update.</p>
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private IdentityServiceClient identityServiceClient;
    @Mock private TeamsServiceClient teamsServiceClient;
    @Mock private SportProfileRepository sportProfileRepository;
    @Mock private IAuditService auditService;

    @InjectMocks private UserServiceImpl service;

    // ── getById ──────────────────────────────────────────────────────────────

    @Test
    void getById_found_returnsModel() {
        UserModel model = UserModel.builder().id(1L).build();
        when(identityServiceClient.getUserById(1L)).thenReturn(model);

        assertEquals(1L, service.getById(1L).getId());
    }

    @Test
    void getById_notFound_throwsResourceNotFoundException() {
        when(identityServiceClient.getUserById(99L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> service.getById(99L));
    }

    // ── getByIdentification ───────────────────────────────────────────────────

    @Test
    void getByIdentification_found_returnsModel() {
        UserModel model = UserModel.builder().id(2L).build();
        when(identityServiceClient.getUserByIdentification("12345")).thenReturn(model);

        assertEquals(2L, service.getByIdentification("12345").getId());
    }

    @Test
    void getByIdentification_notFound_throwsResourceNotFoundException() {
        when(identityServiceClient.getUserByIdentification("xxx")).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> service.getByIdentification("xxx"));
    }

    // ── getAll ────────────────────────────────────────────────────────────────

    @Test
    void getAll_returnsMappedList() {
        when(identityServiceClient.getAllUsers()).thenReturn(List.of(
                UserModel.builder().id(1L).build(),
                UserModel.builder().id(2L).build()));

        assertEquals(2, service.getAll().size());
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_success_logsAuditWhenSportProfileExists() {
        Long id = 1L;
        UserModel existing = UserModel.builder().id(id).status("ACTIVE").build();
        UserModel incoming = UserModel.builder()
                .fullName("New Name")
                .schoolRelation(SchoolRelation.STUDENT)
                .semester(3)
                .build();
        SportProfileEntity sp = SportProfileEntity.builder().id(10L).build();

        when(identityServiceClient.getUserById(id)).thenReturn(existing);
        when(identityServiceClient.updateUser(eq(id), any())).thenReturn(incoming);
        when(sportProfileRepository.findByUserId(id)).thenReturn(Optional.of(sp));

        UserModel result = service.update(id, incoming);

        assertThat(result).isNotNull();
        verify(identityServiceClient).updateUser(eq(id), any());
        verify(auditService).logSportProfile(10L, AuditAction.UPDATE,
                "Admin updated user with id: " + id);
    }

    @Test
    void update_noSportProfile_skipsAuditLog() {
        Long id = 2L;
        UserModel existing = UserModel.builder().id(id).status("ACTIVE").build();
        UserModel incoming = UserModel.builder().fullName("Test").build();

        when(identityServiceClient.getUserById(id)).thenReturn(existing);
        when(identityServiceClient.updateUser(eq(id), any())).thenReturn(incoming);
        when(sportProfileRepository.findByUserId(id)).thenReturn(Optional.empty());

        service.update(id, incoming);

        verify(auditService, never()).logSportProfile(any(), any(), any());
    }

    @Test
    void update_throwsNotFoundWhenMissing() {
        when(identityServiceClient.getUserById(99L)).thenReturn(null);

        assertThatThrownBy(() -> service.update(99L, new UserModel()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: 99");
    }

    @Test
    void update_throwsWhenUserInactive() {
        UserModel existing = UserModel.builder().id(2L).status("INACTIVE").build();
        when(identityServiceClient.getUserById(2L)).thenReturn(existing);

        assertThatThrownBy(() -> service.update(2L, new UserModel()))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Cannot update an inactive user.");
    }

    @Test
    void update_throwsWhenSemesterSetForNonStudent() {
        UserModel existing = UserModel.builder().id(3L).status("ACTIVE").build();
        UserModel incoming = UserModel.builder()
                .schoolRelation(SchoolRelation.ADMINISTRATIVE)
                .semester(2)
                .build();
        when(identityServiceClient.getUserById(3L)).thenReturn(existing);

        assertThatThrownBy(() -> service.update(3L, incoming))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Semester can only be set for students.");
    }

    // ── updateProfile ──────────────────────────────────────────────────────

    @Test
    void updateProfile_delegatesToClient() {
        UserModel existing = UserModel.builder().id(1L).status("ACTIVE").build();
        UserModel updated = UserModel.builder().id(1L).fullName("Nuevo").build();
        when(identityServiceClient.getUserById(1L)).thenReturn(existing);
        when(identityServiceClient.updateUserProfile(eq(1L), any())).thenReturn(updated);

        UserModel result = service.updateProfile(1L, new UserModel());

        assertEquals("Nuevo", result.getFullName());
    }

    @Test
    void updateProfile_notFound_throwsResourceNotFoundException() {
        when(identityServiceClient.getUserById(55L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateProfile(55L, new UserModel()));
    }

    // ── deactivate ───────────────────────────────────────────────────────────

    @Test
    void deactivate_delegatesToClient() {
        UserModel existing = UserModel.builder().id(1L).status("ACTIVE").build();
        when(identityServiceClient.getUserById(1L)).thenReturn(existing);

        service.deactivate(1L);

        verify(identityServiceClient).deactivateUser(1L);
    }

    @Test
    void deactivate_notFound_throwsResourceNotFoundException() {
        when(identityServiceClient.getUserById(5L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> service.deactivate(5L));
    }

    // ── inactivate ─────────────────────────────────────────────────────────

    @Test
    void inactivate_activeUserWithoutTeam_delegatesToClient() {
        UserModel existing = UserModel.builder().id(2L).status("ACTIVE").build();
        when(identityServiceClient.getUserById(2L)).thenReturn(existing);
        when(teamsServiceClient.isPlayerAssignedToTeam(2L)).thenReturn(false);

        service.inactivate(2L);

        verify(identityServiceClient).inactivateUser(2L);
    }

    @Test
    void inactivate_notFound_throwsResourceNotFoundException() {
        when(identityServiceClient.getUserById(99L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> service.inactivate(99L));
        verify(identityServiceClient, never()).inactivateUser(any());
    }

    @Test
    void inactivate_alreadyInactive_throwsBusinessException() {
        UserModel existing = UserModel.builder().id(3L).status("INACTIVE").build();
        when(identityServiceClient.getUserById(3L)).thenReturn(existing);

        assertThrows(BusinessException.class, () -> service.inactivate(3L));
        verify(identityServiceClient, never()).inactivateUser(any());
    }

    @Test
    void inactivate_userInActiveTeam_throwsBusinessException() {
        UserModel existing = UserModel.builder().id(4L).status("ACTIVE").build();
        when(identityServiceClient.getUserById(4L)).thenReturn(existing);
        when(teamsServiceClient.isPlayerAssignedToTeam(4L)).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.inactivate(4L));
        verify(identityServiceClient, never()).inactivateUser(any());
    }

    // ── search ───────────────────────────────────────────────────────────────

    @Test
    void search_withAllParams_normalizesAndCallsClient() {
        UserModel m1 = UserModel.builder().id(1L).build();
        SportProfileEntity sp = SportProfileEntity.builder().userId(1L).position("FORWARD").build();
        when(identityServiceClient.searchUsers("juan", "ACTIVE")).thenReturn(List.of(m1));
        when(sportProfileRepository.findByPosition("FORWARD")).thenReturn(List.of(sp));

        List<UserModel> result = service.search(" juan ", "forward", "active");

        assertEquals(1, result.size());
        verify(identityServiceClient).searchUsers("juan", "ACTIVE");
    }

    @Test
    void search_withNullParams_passesNullsToClient() {
        when(identityServiceClient.searchUsers(null, null)).thenReturn(List.of());

        List<UserModel> result = service.search(null, null, null);

        assertTrue(result.isEmpty());
        verify(identityServiceClient).searchUsers(null, null);
    }

    @Test
    void search_withPosition_filtersToMatchingUsers() {
        UserModel u1 = UserModel.builder().id(1L).build();
        UserModel u2 = UserModel.builder().id(2L).build();
        SportProfileEntity sp = SportProfileEntity.builder().userId(1L).position("GOALKEEPER").build();

        when(identityServiceClient.searchUsers(null, null)).thenReturn(List.of(u1, u2));
        when(sportProfileRepository.findByPosition("GOALKEEPER")).thenReturn(List.of(sp));

        List<UserModel> result = service.search(null, "GOALKEEPER", null);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }
}
