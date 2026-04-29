package edu.dosw.users.mapper;

import edu.dosw.users.dto.UserProfileRequest;
import edu.dosw.users.dto.UserProfileResponse;
import edu.dosw.users.entity.UserProfileEntity;
import edu.dosw.users.enums.Gender;
import edu.dosw.users.enums.SchoolRelation;
import edu.dosw.users.model.UserProfileModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link UserProfileMapper}.
 *
 * <p>Verifies bidirectional conversion between {@link UserProfileEntity} and
 * {@link UserProfileModel}, including the mapping of the {@link Gender} and
 * {@link SchoolRelation} enums to and from their {@code String} representations,
 * handling of null optional fields, and the fact that {@code sportProfile} is
 * ignored in {@code toEntity}.</p>
 */
class UserProfileMapperTest {

    private UserProfileMapper mapper;

    /**
     * Initialises the MapStruct-generated implementation before each test.
     */
    @BeforeEach
    void setUp() {
        mapper = new UserProfileMapperImpl();
    }

    // ── toModel ──────────────────────────────────────────────────────────────

    /**
     * Verifies that {@code toModel} correctly maps all entity fields to the
     * model, including conversion from {@code String} to {@link Gender} and
     * {@link SchoolRelation}.
     */
    @Test
    void toModel_mapsAllFields() {
        UserProfileEntity entity = UserProfileEntity.builder()
                .id(1L)
                .fullName("Carlos Perez")
                .email("carlos@escuelaing.edu.co")
                .password("hashed")
                .identification("12345678")
                .birthDate(LocalDate.of(2000, 5, 15))
                .gender("MALE")
                .schoolRelation("STUDENT")
                .academicProgram("Ingeniería de Sistemas")
                .semester(6)
                .status("ACTIVE")
                .profileCreatedAt(LocalDateTime.of(2024, 1, 10, 8, 0))
                .updatedAt(LocalDateTime.of(2024, 3, 1, 10, 30))
                .build();

        UserProfileModel model = mapper.toModel(entity);

        assertNotNull(model);
        assertEquals(1L, model.getId());
        assertEquals("Carlos Perez", model.getFullName());
        assertEquals("carlos@escuelaing.edu.co", model.getEmail());
        assertEquals("hashed", model.getPassword());
        assertEquals("12345678", model.getIdentification());
        assertEquals(LocalDate.of(2000, 5, 15), model.getBirthDate());
        assertEquals(Gender.MALE, model.getGender());
        assertEquals(SchoolRelation.STUDENT, model.getSchoolRelation());
        assertEquals("Ingeniería de Sistemas", model.getAcademicProgram());
        assertEquals(6, model.getSemester());
        assertEquals("ACTIVE", model.getStatus());
        assertEquals(LocalDateTime.of(2024, 1, 10, 8, 0), model.getProfileCreatedAt());
        assertEquals(LocalDateTime.of(2024, 3, 1, 10, 30), model.getUpdatedAt());
    }

    /**
     * Verifies that when {@code gender} and {@code schoolRelation} are {@code null}
     * in the entity, the corresponding model fields are also {@code null}.
     */
    @Test
    void toModel_nullGenderAndSchoolRelation_mapsToNull() {
        UserProfileEntity entity = UserProfileEntity.builder()
                .id(2L)
                .fullName("Sin datos opcionales")
                .email("test@test.com")
                .password("pass")
                .identification("99999999")
                .status("ACTIVE")
                .build();

        UserProfileModel model = mapper.toModel(entity);

        assertNull(model.getGender());
        assertNull(model.getSchoolRelation());
    }

    /**
     * Verifies that {@code toModel} returns {@code null} when given a
     * {@code null} entity.
     */
    @Test
    void toModel_nullEntity_returnsNull() {
        assertNull(mapper.toModel((UserProfileEntity) null));
    }

    // ── toEntity ─────────────────────────────────────────────────────────────

    /**
     * Verifies that {@code toEntity} correctly maps all model fields to the
     * entity, including conversion of {@link Gender} and {@link SchoolRelation}
     * to their {@code String} representation.
     */
    @Test
    void toEntity_mapsAllFields() {
        UserProfileModel model = UserProfileModel.builder()
                .id(1L)
                .fullName("Carlos Perez")
                .email("carlos@escuelaing.edu.co")
                .password("hashed")
                .identification("12345678")
                .birthDate(LocalDate.of(2000, 5, 15))
                .gender(Gender.FEMALE)
                .schoolRelation(SchoolRelation.PROFESSOR)
                .academicProgram("Ingeniería de Sistemas")
                .semester(6)
                .status("ACTIVE")
                .profileCreatedAt(LocalDateTime.of(2024, 1, 10, 8, 0))
                .updatedAt(LocalDateTime.of(2024, 3, 1, 10, 30))
                .build();

        UserProfileEntity entity = mapper.toEntity(model);

        assertNotNull(entity);
        assertEquals(1L, entity.getId());
        assertEquals("Carlos Perez", entity.getFullName());
        assertEquals("FEMALE", entity.getGender());
        assertEquals("PROFESSOR", entity.getSchoolRelation());
        assertEquals("ACTIVE", entity.getStatus());
        assertNull(entity.getSportProfile());
    }

    /**
     * Verifies that the {@code sportProfile} relationship is ignored by the
     * mapper and is left as {@code null} in the resulting entity.
     */
    @Test
    void toEntity_sportProfile_isIgnored() {
        UserProfileModel model = UserProfileModel.builder()
                .id(1L)
                .fullName("Test")
                .email("t@t.com")
                .password("p")
                .identification("1")
                .status("ACTIVE")
                .build();

        UserProfileEntity entity = mapper.toEntity(model);

        assertNull(entity.getSportProfile());
    }

    /**
     * Verifies that {@code toEntity} returns {@code null} when given a
     * {@code null} model.
     */
    @Test
    void toEntity_nullModel_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    // ── toModel(Request) ─────────────────────────────────────────────────────

    @Test
    void toModel_fromRequest_mapsFieldsAndIgnoresMetadata() {
        UserProfileRequest request = UserProfileRequest.builder()
                .fullName("Ana García")
                .email("ana@escuelaing.edu.co")
                .password("hashed")
                .identification("87654321")
                .birthDate(LocalDate.of(1999, 3, 20))
                .gender(Gender.FEMALE)
                .schoolRelation(SchoolRelation.STUDENT)
                .academicProgram("Ingeniería Civil")
                .semester(4)
                .build();

        UserProfileModel model = mapper.toModel(request);

        assertNotNull(model);
        assertNull(model.getId());
        assertNull(model.getStatus());
        assertNull(model.getProfileCreatedAt());
        assertNull(model.getUpdatedAt());
        assertEquals("Ana García", model.getFullName());
        assertEquals("ana@escuelaing.edu.co", model.getEmail());
        assertEquals("hashed", model.getPassword());
        assertEquals("87654321", model.getIdentification());
        assertEquals(LocalDate.of(1999, 3, 20), model.getBirthDate());
        assertEquals(Gender.FEMALE, model.getGender());
        assertEquals(SchoolRelation.STUDENT, model.getSchoolRelation());
        assertEquals("Ingeniería Civil", model.getAcademicProgram());
        assertEquals(4, model.getSemester());
    }

    // ── toResponse ───────────────────────────────────────────────────────────

    @Test
    void toResponse_mapsAllFieldsExceptPassword() {
        UserProfileModel model = UserProfileModel.builder()
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

        UserProfileResponse response = mapper.toResponse(model);

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