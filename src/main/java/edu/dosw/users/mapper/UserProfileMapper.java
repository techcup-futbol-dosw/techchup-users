package edu.dosw.users.mapper;

import edu.dosw.users.dto.UserProfileRequest;
import edu.dosw.users.dto.UserProfileResponse;
import edu.dosw.users.entity.UserProfileEntity;
import edu.dosw.users.model.UserProfileModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for bidirectional conversion between
 * {@link UserProfileEntity} and {@link UserProfileModel}.
 *
 * <p>When converting to an entity, the {@code sportProfile} relationship is
 * ignored and must be managed independently by the service layer to avoid
 * update cycles.</p>
 */
@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    /**
     * Converts a user profile entity to its domain model.
     *
     * @param entity source entity; may be {@code null}
     * @return resulting model, or {@code null} if the entity is {@code null}
     */
    UserProfileModel toModel(UserProfileEntity entity);

    /**
     * Converts a user profile model to its JPA entity.
     * The {@code sportProfile} relationship is left as {@code null} and must
     * be assigned by the service layer.
     *
     * @param model source model; may be {@code null}
     * @return resulting entity, or {@code null} if the model is {@code null}
     */
    @Mapping(target = "sportProfile", ignore = true)
    UserProfileEntity toEntity(UserProfileModel model);

    /** Converts a {@link UserProfileRequest} to its domain model. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "profileCreatedAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserProfileModel toModel(UserProfileRequest request);

    /** Converts a domain model to a {@link UserProfileResponse} (no password). */
    UserProfileResponse toResponse(UserProfileModel model);
}