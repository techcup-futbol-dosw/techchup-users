package edu.dosw.users.mapper;

import edu.dosw.users.dto.AdminUserUpdateRequest;
import edu.dosw.users.dto.UserProfileUpdateRequest;
import edu.dosw.users.dto.UserResponse;
import edu.dosw.users.enums.Gender;
import edu.dosw.users.enums.SchoolRelation;
import edu.dosw.users.model.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link UserMapper}.
 *
 * <p>Verifies conversion between user-facing DTOs and {@link UserModel},
 * including field ignoring rules for protected and computed fields.</p>
 */
class UserMapperTest {

    private UserMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserMapperImpl();
    }

    // ── toModel(AdminUserUpdateRequest) ──────────────────────────────────────

    @Test
    void toModel_fromAdminUpdateRequest_mapsAllowedFieldsOnly() {
        AdminUserUpdateRequest request = AdminUserUpdateRequest.builder()
                .fullName("Nuevo Nombre")
                .schoolRelation(SchoolRelation.STUDENT)
                .academicProgram("Ingeniería de Sistemas")
                .semester(5)
                .build();

        UserModel model = mapper.toModel(request);

        assertNotNull(model);
        assertNull(model.getId());
        assertNull(model.getEmail());
        assertNull(model.getPassword());
        assertNull(model.getIdentification());
        assertNull(model.getStatus());
        assertEquals("Nuevo Nombre", model.getFullName());
        assertEquals(SchoolRelation.STUDENT, model.getSchoolRelation());
        assertEquals("Ingeniería de Sistemas", model.getAcademicProgram());
        assertEquals(5, model.getSemester());
    }

    // ── toModel(UserProfileUpdateRequest) ────────────────────────────────────

    @Test
    void toModel_fromProfileUpdateRequest_mapsFieldsAndIgnoresCredentials() {
        UserProfileUpdateRequest request = UserProfileUpdateRequest.builder()
                .fullName("Juliana Gomez")
                .identification("555666")
                .birthDate(LocalDate.of(1998, 9, 12))
                .gender(Gender.FEMALE)
                .schoolRelation(SchoolRelation.GRADUATE)
                .academicProgram("Ingeniería Industrial")
                .semester(8)
                .build();

        UserModel model = mapper.toModel(request);

        assertNotNull(model);
        assertNull(model.getId());
        assertNull(model.getStatus());
        assertNull(model.getProfileCreatedAt());
        assertNull(model.getUpdatedAt());
        assertNull(model.getEmail());
        assertNull(model.getPassword());
        assertEquals("Juliana Gomez", model.getFullName());
        assertEquals("555666", model.getIdentification());
        assertEquals(LocalDate.of(1998, 9, 12), model.getBirthDate());
        assertEquals(Gender.FEMALE, model.getGender());
        assertEquals(SchoolRelation.GRADUATE, model.getSchoolRelation());
        assertEquals("Ingeniería Industrial", model.getAcademicProgram());
        assertEquals(8, model.getSemester());
    }

    // ── toResponse ───────────────────────────────────────────────────────────

    @Test
    void toResponse_mapsAllFieldsExceptPassword() {
        UserModel model = UserModel.builder()
                .id(7L)
                .fullName("Luis Mora")
                .email("luis@escuelaing.edu.co")
                .password("should-be-excluded")
                .identification("11223344")
                .birthDate(LocalDate.of(2001, 7, 5))
                .gender(Gender.MALE)
                .schoolRelation(SchoolRelation.PROFESSOR)
                .academicProgram("Matemáticas")
                .semester(8)
                .status("ACTIVE")
                .profileCreatedAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                .updatedAt(LocalDateTime.of(2024, 6, 1, 0, 0))
                .build();

        UserResponse response = mapper.toResponse(model);

        assertNotNull(response);
        assertEquals(7L, response.getId());
        assertEquals("Luis Mora", response.getFullName());
        assertEquals("luis@escuelaing.edu.co", response.getEmail());
        assertEquals("11223344", response.getIdentification());
        assertEquals(LocalDate.of(2001, 7, 5), response.getBirthDate());
        assertEquals(Gender.MALE, response.getGender());
        assertEquals(SchoolRelation.PROFESSOR, response.getSchoolRelation());
        assertEquals("Matemáticas", response.getAcademicProgram());
        assertEquals(8, response.getSemester());
        assertEquals("ACTIVE", response.getStatus());
        assertEquals(LocalDateTime.of(2024, 1, 1, 0, 0), response.getProfileCreatedAt());
        assertEquals(LocalDateTime.of(2024, 6, 1, 0, 0), response.getUpdatedAt());
    }

    @Test
    void toResponse_nullModel_returnsNull() {
        assertNull(mapper.toResponse(null));
    }
}
