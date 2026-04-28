package edu.dosw.users.mapper;

import edu.dosw.users.entity.SportProfileEntity;
import edu.dosw.users.entity.UserProfileEntity;
import edu.dosw.users.enums.Position;
import edu.dosw.users.model.SportProfileModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link SportProfileMapper}.
 *
 * <p>Verifies bidirectional conversion between {@link SportProfileEntity}
 * and {@link SportProfileModel}, including the mapping of the user identifier
 * nested in {@code UserProfileEntity} and the handling of null values.</p>
 */
class SportProfileMapperTest {

    private SportProfileMapper mapper;

    /**
     * Initialises the MapStruct-generated implementation before each test.
     */
    @BeforeEach
    void setUp() {
        mapper = new SportProfileMapperImpl();
    }

    

    /**
     * Verifies that {@code toModel} correctly maps all entity fields to the
     * model, including extraction of {@code userProfile.id} into {@code userId}.
     */
    @Test
    void toModel_mapsUserProfileIdToUserId() {
        UserProfileEntity user = UserProfileEntity.builder().id(42L).build();
        SportProfileEntity entity = SportProfileEntity.builder()
                .id(1L)
                .userProfile(user)
                .position("GOALKEEPER")
                .dorsalNumber(1)
                .photoId("abc123def456abc123def456")
                .available(true)
                .createdAt(LocalDateTime.of(2024, 2, 1, 9, 0))
                .updatedAt(LocalDateTime.of(2024, 3, 1, 9, 0))
                .build();

        SportProfileModel model = mapper.toModel(entity);

        assertNotNull(model);
        assertEquals(1L, model.getId());
        assertEquals(42L, model.getUserId());
        assertEquals(Position.GOALKEEPER, model.getPosition());
        assertEquals(1, model.getDorsalNumber());
        assertEquals("abc123def456abc123def456", model.getPhotoId());
        assertTrue(model.isAvailable());
    }

    /**
     * Verifies that when {@code userProfile} is {@code null}, the {@code userId}
     * field of the resulting model is also {@code null}.
     */
    @Test
    void toModel_nullUserProfile_userIdIsNull() {
        SportProfileEntity entity = SportProfileEntity.builder()
                .id(1L)
                .userProfile(null)
                .position("DEFENDER")
                .available(false)
                .build();

        SportProfileModel model = mapper.toModel(entity);

        assertNull(model.getUserId());
        assertEquals(Position.DEFENDER, model.getPosition());
    }

    /**
     * Verifies that {@code toModel} returns {@code null} when given a
     * {@code null} entity.
     */
    @Test
    void toModel_nullEntity_returnsNull() {
        assertNull(mapper.toModel(null));
    }


    /**
     * Verifies that {@code toEntity} correctly maps all model fields to the
     * entity and that {@code userProfile} is left as {@code null} (field
     * ignored by the mapper).
     */
    @Test
    void toEntity_mapsFields_andIgnoresUserProfile() {
        SportProfileModel model = SportProfileModel.builder()
                .id(1L)
                .userId(42L)
                .position(Position.FORWARD)
                .dorsalNumber(9)
                .photoId("abc123def456abc123def456")
                .available(true)
                .createdAt(LocalDateTime.of(2024, 2, 1, 9, 0))
                .updatedAt(LocalDateTime.of(2024, 3, 1, 9, 0))
                .build();

        SportProfileEntity entity = mapper.toEntity(model);

        assertNotNull(entity);
        assertEquals(1L, entity.getId());
        assertEquals("FORWARD", entity.getPosition());
        assertEquals(9, entity.getDorsalNumber());
        assertTrue(entity.isAvailable());
        assertNull(entity.getUserProfile());
    }

    /**
     * Verifies that {@code toEntity} returns {@code null} when given a
     * {@code null} model.
     */
    @Test
    void toEntity_nullModel_returnsNull() {
        assertNull(mapper.toEntity(null));
    }
}