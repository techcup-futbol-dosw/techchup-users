package edu.dosw.users.mapper;

import edu.dosw.users.dto.SportProfileRequest;
import edu.dosw.users.dto.SportProfileResponse;
import edu.dosw.users.entity.SportProfileEntity;
import edu.dosw.users.model.SportProfileModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for bidirectional conversion between
 * {@link SportProfileEntity} and {@link SportProfileModel}.
 *
 * <p>When converting to a model, extracts the user profile identifier
 * ({@code userProfile.id}) and assigns it to the {@code userId} field of the
 * model. When converting to an entity, the {@code userProfile} relationship
 * is ignored and must be resolved by the service layer.</p>
 */
@Mapper(componentModel = "spring")
public interface SportProfileMapper {

    /**
     * Converts a sport profile entity to its domain model.
     *
     * @param entity source entity; may be {@code null}
     * @return resulting model, or {@code null} if the entity is {@code null}
     */
    @Mapping(source = "userProfile.id", target = "userId")
    SportProfileModel toModel(SportProfileEntity entity);

    /**
     * Converts a sport profile model to its JPA entity.
     * The {@code userProfile} relationship is left as {@code null} and must
     * be assigned by the service layer.
     *
     * @param model source model; may be {@code null}
     * @return resulting entity, or {@code null} if the model is {@code null}
     */
    @Mapping(target = "userProfile", ignore = true)
    SportProfileEntity toEntity(SportProfileModel model);

    /** Converts a {@link SportProfileRequest} to its domain model. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "photoId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SportProfileModel toModel(SportProfileRequest request);

    /** Converts a domain model to a {@link SportProfileResponse}. */
    SportProfileResponse toResponse(SportProfileModel model);
}