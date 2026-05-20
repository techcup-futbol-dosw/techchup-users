package edu.dosw.users.mapper;

import edu.dosw.users.dto.SportProfileRequest;
import edu.dosw.users.dto.SportProfileResponse;
import edu.dosw.users.entity.SportProfileEntity;
import edu.dosw.users.model.SportProfileModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper MapStruct para la conversión bidireccional entre {@link SportProfileEntity} y {@link SportProfileModel}.
 *
 * <p>El campo {@code userId} se mapea directamente ya que tanto la entidad como el modelo
 * comparten el mismo nombre de campo. También convierte desde {@link SportProfileRequest}
 * ignorando los campos gestionados por el servicio (id, userId, photoId, timestamps).</p>
 *
 * @author CodeForge
 * @since 1.0
 */
@Mapper(componentModel = "spring")
public interface SportProfileMapper {

    /**
     * Convierte una entidad de perfil deportivo a su modelo de dominio.
     *
     * @param entity entidad fuente; puede ser {@code null}
     * @return modelo resultante, o {@code null} si la entidad es {@code null}
     */
    SportProfileModel toModel(SportProfileEntity entity);

    /**
     * Convierte un modelo de perfil deportivo a su entidad JPA.
     *
     * @param model modelo fuente; puede ser {@code null}
     * @return entidad resultante, o {@code null} si el modelo es {@code null}
     */
    SportProfileEntity toEntity(SportProfileModel model);

    /**
     * Convierte un {@link SportProfileRequest} a su modelo de dominio.
     * Los campos {@code id}, {@code userId}, {@code photoId} y los timestamps son
     * ignorados y deben ser asignados por la capa de servicio.
     *
     * @param request DTO de solicitud fuente; puede ser {@code null}
     * @return modelo resultante, o {@code null} si el request es {@code null}
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "photoId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SportProfileModel toModel(SportProfileRequest request);

    /**
     * Convierte un modelo de dominio a un {@link SportProfileResponse}.
     *
     * @param model modelo fuente; puede ser {@code null}
     * @return DTO de respuesta resultante, o {@code null} si el modelo es {@code null}
     */
    SportProfileResponse toResponse(SportProfileModel model);
}
