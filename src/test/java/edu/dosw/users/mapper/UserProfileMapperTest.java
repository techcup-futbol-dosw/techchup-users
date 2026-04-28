package edu.dosw.users.mapper;

import edu.dosw.users.entity.UserProfileEntity;
import edu.dosw.users.enums.Gender;
import edu.dosw.users.enums.SchoolRelation;
import edu.dosw.users.model.UserProfileModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserProfileMapperTest {

    private UserProfileMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new UserProfileMapperImpl();
    }

    // ── toModel ──────────────────────────────────────────────────────────────

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

    @Test
    void toModel_nullEntity_returnsNull() {
        assertNull(mapper.toModel(null));
    }

    // ── toEntity ─────────────────────────────────────────────────────────────

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

    @Test
    void toEntity_nullModel_returnsNull() {
        assertNull(mapper.toEntity(null));
    }
}