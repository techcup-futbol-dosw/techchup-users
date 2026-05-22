package edu.dosw.users.mapper;

import edu.dosw.users.dto.AdminUserUpdateRequest;
import edu.dosw.users.dto.UserProfileUpdateRequest;
import edu.dosw.users.dto.UserResponse;
import edu.dosw.users.entity.UserEntity;
import edu.dosw.users.model.UserModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper MapStruct para la conversión entre los DTOs orientados al usuario y el
 * modelo de dominio {@link UserModel}.
 *
 * <p>Los datos de usuario son propiedad del servicio de identidad; este mapper solo
 * gestiona las conversiones DTO ↔ modelo necesarias en la capa de controlador.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Convierte un request de actualización de administrador a un modelo de dominio que
     * contiene únicamente los campos que el administrador tiene permitido modificar.
     * Los campos {@code id}, {@code email}, {@code password}, {@code identification},
     * {@code birthDate}, {@code gender}, {@code status} y los timestamps se ignoran.
     *
     * @param request DTO de actualización con privilegios de administrador; puede ser {@code null}
     * @return modelo resultante, o {@code null} si el request es {@code null}
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

    /**
     * Convierte un {@link UserProfileUpdateRequest} a su modelo de dominio.
     * Los campos {@code id}, {@code email}, {@code password}, {@code status} y
     * los timestamps se ignoran para que el usuario no pueda alterar datos sensibles.
     *
     * @param request DTO de actualización de perfil propio; puede ser {@code null}
     * @return modelo resultante, o {@code null} si el request es {@code null}
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "profileCreatedAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserModel toModel(UserProfileUpdateRequest request);

    /**
     * Convierte un modelo de dominio a un {@link UserResponse}.
     * El campo {@code password} no se incluye en la respuesta.
     *
     * @param model modelo fuente; puede ser {@code null}
     * @return DTO de respuesta resultante, o {@code null} si el modelo es {@code null}
     */
    UserResponse toResponse(UserModel model);

    /**
     * Convierte una entidad JPA {@link UserEntity} al modelo de dominio {@link UserModel}.
     *
     * @param entity entidad fuente; puede ser {@code null}
     * @return modelo resultante, o {@code null} si la entidad es {@code null}
     */
    UserModel toModel(UserEntity entity);

    /**
     * Convierte un modelo de dominio {@link UserModel} a entidad JPA {@link UserEntity}.
     *
     * @param model modelo fuente; puede ser {@code null}
     * @return entidad resultante, o {@code null} si el modelo es {@code null}
     */
    UserEntity toEntity(UserModel model);
}
