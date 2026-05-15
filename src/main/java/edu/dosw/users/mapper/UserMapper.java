package edu.dosw.users.mapper;

import edu.dosw.users.dto.AdminUserUpdateRequest;
import edu.dosw.users.dto.UserProfileUpdateRequest;
import edu.dosw.users.dto.UserResponse;
import edu.dosw.users.model.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for converting between user-facing DTOs and the
 * {@link UserModel} domain model.
 *
 * <p>User data is owned by the identity service; this mapper only handles
 * DTO ↔ model conversions needed by the controller layer.</p>
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Converts an administrator update request to a domain model containing
     * only the fields that are permitted to be changed by an administrator.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "identification", ignore = true)
    @Mapping(target = "birthDate", ignore = true)
    @Mapping(target = "gender", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "profileCreatedAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserModel toModel(AdminUserUpdateRequest request);

    /** Converts a {@link UserProfileUpdateRequest} to its domain model. */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "profileCreatedAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserModel toModel(UserProfileUpdateRequest request);

    /** Converts a domain model to a {@link UserResponse} (no password). */
    UserResponse toResponse(UserModel model);
}
