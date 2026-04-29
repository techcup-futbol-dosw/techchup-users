package edu.dosw.users.mapper;

import edu.dosw.users.dto.UserRequest;
import edu.dosw.users.dto.UserResponse;
import edu.dosw.users.entity.UserEntity;
import edu.dosw.users.model.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for bidirectional conversion between
 * {@link UserEntity} and {@link UserModel}.
 *
 * <p>When converting to an entity, the {@code sportProfile} relationship is
 * ignored and must be managed independently by the service layer to avoid
 * update cycles.</p>
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Converts a user profile entity to its domain model.
     *
     * @param entity source entity; may be {@code null}
     * @return resulting model, or {@code null} if the entity is {@code null}
     */
    UserModel toModel(UserEntity entity);

    /**
     * Converts a user profile model to its JPA entity.
     * The {@code sportProfile} relationship is left as {@code null} and must
     * be assigned by the service layer.
     *
     * @param model source model; may be {@code null}
     * @return resulting entity, or {@code null} if the model is {@code null}
     */
    @Mapping(target = "sportProfile", ignore = true)
    UserEntity toEntity(UserModel model);

    /** Converts a {@link UserRequest} to its domain model. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "profileCreatedAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserModel toModel(UserRequest request);

    /** Converts a domain model to a {@link UserResponse} (no password). */
    UserResponse toResponse(UserModel model);
}