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
 * <p>The {@code userId} field is mapped directly since both entity and model
 * share the same field name.</p>
 */
@Mapper(componentModel = "spring")
public interface SportProfileMapper {

    /**
     * Converts a sport profile entity to its domain model.
     *
     * @param entity source entity; may be {@code null}
     * @return resulting model, or {@code null} if the entity is {@code null}
     */
    SportProfileModel toModel(SportProfileEntity entity);

    /**
     * Converts a sport profile model to its JPA entity.
     *
     * @param model source model; may be {@code null}
     * @return resulting entity, or {@code null} if the model is {@code null}
     */
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
