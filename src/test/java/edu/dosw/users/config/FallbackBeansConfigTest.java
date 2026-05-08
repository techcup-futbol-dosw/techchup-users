package edu.dosw.users.config;

import edu.dosw.users.client.IdentityServiceClient;
import edu.dosw.users.enums.SchoolRelation;
import edu.dosw.users.exception.ResourceNotFoundException;
import edu.dosw.users.model.UserModel;
import edu.dosw.users.service.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link FallbackBeansConfig}.
 *
 * <p>Exercises the in-memory stub beans directly — without a Spring context —
 * so that the stub logic is covered even though integration tests replace these
 * beans with {@code @MockitoBean}.</p>
 */
class FallbackBeansConfigTest {

    private IdentityServiceClient identityClient;
    private ImageService imageService;

    @BeforeEach
    void setUp() {
        FallbackBeansConfig config = new FallbackBeansConfig();
        identityClient = config.identityServiceClientStub();
        imageService = config.imageServiceStub();
    }

    // ── imageServiceStub ──────────────────────────────────────────────────────

    @Test
    void imageStub_upload_returnsNull() {
        assertNull(imageService.upload(null, 1L));
    }

    @Test
    void imageStub_getPhoto_returnsNull() {
        assertNull(imageService.getPhoto("any-id"));
    }

    @Test
    void imageStub_delete_isNoOp() {
        assertDoesNotThrow(() -> imageService.delete("any-id"));
    }

    // ── createUser ────────────────────────────────────────────────────────────

    @Test
    void createUser_assignsIdAndDefaultsWhenNull() {
        UserModel model = UserModel.builder().fullName("Test").identification("ID-001").build();
        UserModel result = identityClient.createUser(model);

        assertNotNull(result.getId());
        assertEquals("ACTIVE", result.getStatus());
        assertNotNull(result.getProfileCreatedAt());
        assertNotNull(result.getUpdatedAt());
    }

    @Test
    void createUser_keepsExistingStatusAndTimestamps() {
        LocalDateTime ts = LocalDateTime.of(2024, 1, 1, 0, 0);
        UserModel model = UserModel.builder()
                .fullName("Custom")
                .status("INACTIVE")
                .profileCreatedAt(ts)
                .updatedAt(ts)
                .build();
        UserModel result = identityClient.createUser(model);

        assertEquals("INACTIVE", result.getStatus());
        assertEquals(ts, result.getProfileCreatedAt());
    }

    @Test
    void createUser_withoutIdentification_doesNotRegisterInIdentificationIndex() {
        UserModel model = UserModel.builder().fullName("Sin ID").build();
        UserModel result = identityClient.createUser(model);

        assertNotNull(result.getId());
        assertNull(identityClient.getUserByIdentification("NOEXISTE"));
    }

    // ── userExists ────────────────────────────────────────────────────────────

    @Test
    void userExists_returnsTrueForCreatedUser() {
        UserModel created = identityClient.createUser(UserModel.builder().fullName("A").build());
        assertTrue(identityClient.userExists(created.getId()));
    }

    @Test
    void userExists_returnsFalseForUnknownId() {
        assertFalse(identityClient.userExists(9999L));
    }

    // ── getUserById ───────────────────────────────────────────────────────────

    @Test
    void getUserById_returnsUserWhenFound() {
        UserModel created = identityClient.createUser(UserModel.builder().fullName("B").build());

        UserModel found = identityClient.getUserById(created.getId());

        assertNotNull(found);
        assertEquals("B", found.getFullName());
    }

    @Test
    void getUserById_returnsNullWhenNotFound() {
        assertNull(identityClient.getUserById(9999L));
    }

    // ── getUserByIdentification ───────────────────────────────────────────────

    @Test
    void getUserByIdentification_returnsUserWhenFound() {
        identityClient.createUser(UserModel.builder().fullName("C").identification("ID-002").build());

        UserModel found = identityClient.getUserByIdentification("ID-002");

        assertNotNull(found);
        assertEquals("ID-002", found.getIdentification());
    }

    @Test
    void getUserByIdentification_returnsNullWhenNotFound() {
        assertNull(identityClient.getUserByIdentification("NOEXISTE"));
    }

    // ── getAllUsers ───────────────────────────────────────────────────────────

    @Test
    void getAllUsers_returnsAllCreatedUsers() {
        identityClient.createUser(UserModel.builder().fullName("D1").build());
        identityClient.createUser(UserModel.builder().fullName("D2").build());

        assertEquals(2, identityClient.getAllUsers().size());
    }

    // ── updateUser ────────────────────────────────────────────────────────────

    @Test
    void updateUser_updatesAllProvidedFields() {
        UserModel created = identityClient.createUser(UserModel.builder().fullName("E").build());

        identityClient.updateUser(created.getId(), UserModel.builder()
                .fullName("E Updated")
                .schoolRelation(SchoolRelation.STUDENT)
                .academicProgram("Ingeniería")
                .semester(3)
                .build());

        UserModel updated = identityClient.getUserById(created.getId());
        assertEquals("E Updated", updated.getFullName());
        assertEquals(SchoolRelation.STUDENT, updated.getSchoolRelation());
        assertEquals("Ingeniería", updated.getAcademicProgram());
        assertEquals(3, updated.getSemester());
    }

    @Test
    void updateUser_withNullFields_keepsExistingValues() {
        UserModel created = identityClient.createUser(
                UserModel.builder().fullName("F").semester(5).build());

        identityClient.updateUser(created.getId(), UserModel.builder().build());

        UserModel after = identityClient.getUserById(created.getId());
        assertEquals("F", after.getFullName());
        assertEquals(5, after.getSemester());
    }

    @Test
    void updateUser_notFound_throwsResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class,
                () -> identityClient.updateUser(9999L, UserModel.builder().fullName("X").build()));
    }

    // ── updateUserProfile ─────────────────────────────────────────────────────

    @Test
    void updateUserProfile_delegatesToUpdateUser() {
        UserModel created = identityClient.createUser(UserModel.builder().fullName("G").build());

        identityClient.updateUserProfile(created.getId(), UserModel.builder().fullName("G Updated").build());

        assertEquals("G Updated", identityClient.getUserById(created.getId()).getFullName());
    }

    // ── deactivateUser ────────────────────────────────────────────────────────

    @Test
    void deactivateUser_setsStatusToInactive() {
        UserModel created = identityClient.createUser(UserModel.builder().fullName("H").build());

        identityClient.deactivateUser(created.getId());

        assertEquals("INACTIVE", identityClient.getUserById(created.getId()).getStatus());
    }

    @Test
    void deactivateUser_notFound_throwsResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class,
                () -> identityClient.deactivateUser(9999L));
    }

    // ── inactivateUser ────────────────────────────────────────────────────────

    @Test
    void inactivateUser_setsStatusToInactive() {
        UserModel created = identityClient.createUser(UserModel.builder().fullName("I").build());

        identityClient.inactivateUser(created.getId());

        assertEquals("INACTIVE", identityClient.getUserById(created.getId()).getStatus());
    }

    // ── searchUsers ───────────────────────────────────────────────────────────

    @Test
    void searchUsers_withName_returnsMatchingUsers() {
        identityClient.createUser(UserModel.builder().fullName("Juan Perez").build());
        identityClient.createUser(UserModel.builder().fullName("Maria Lopez").build());

        List<UserModel> result = identityClient.searchUsers("Juan", null);

        assertEquals(1, result.size());
        assertEquals("Juan Perez", result.get(0).getFullName());
    }

    @Test
    void searchUsers_withStatus_returnsOnlyMatchingStatus() {
        UserModel active = identityClient.createUser(UserModel.builder().fullName("Active").build());
        UserModel inactive = identityClient.createUser(UserModel.builder().fullName("Inactive").build());
        identityClient.deactivateUser(inactive.getId());

        List<UserModel> result = identityClient.searchUsers(null, "ACTIVE");

        assertEquals(1, result.size());
        assertEquals(active.getId(), result.get(0).getId());
    }

    @Test
    void searchUsers_withBothParams_appliesBothFilters() {
        identityClient.createUser(UserModel.builder().fullName("Juan").build());
        UserModel juana = identityClient.createUser(UserModel.builder().fullName("Juana").build());
        identityClient.deactivateUser(juana.getId());

        List<UserModel> result = identityClient.searchUsers("Juan", "ACTIVE");

        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getFullName());
    }

    @Test
    void searchUsers_withNullParams_returnsAll() {
        identityClient.createUser(UserModel.builder().fullName("U1").build());
        identityClient.createUser(UserModel.builder().fullName("U2").build());

        assertEquals(2, identityClient.searchUsers(null, null).size());
    }

    @Test
    void searchUsers_withNameFilter_userWithNullFullName_isExcluded() {
        identityClient.createUser(UserModel.builder().build()); // fullName = null
        identityClient.createUser(UserModel.builder().fullName("Juan").build());

        List<UserModel> result = identityClient.searchUsers("Juan", null);

        assertEquals(1, result.size());
        assertEquals("Juan", result.get(0).getFullName());
    }
}