package edu.dosw.users.mapper;

import edu.dosw.users.dto.InvitationResponse;
import edu.dosw.users.entity.InvitationEntity;
import edu.dosw.users.model.InvitationModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper MapStruct para la conversión bidireccional entre {@link InvitationEntity} y {@link InvitationModel}.
 *
 * <p>La entidad almacena el identificador del jugador en un campo llamado {@code userId}
 * (columna {@code player_id}), mientras que el modelo utiliza {@code playerId} por
 * compatibilidad con la API. Ambas direcciones se mapean de forma explícita.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
@Mapper(componentModel = "spring")
public interface InvitationMapper {

    /**
     * Convierte una entidad de invitación a su modelo de dominio.
     *
     * @param entity entidad fuente; puede ser {@code null}
     * @return modelo resultante, o {@code null} si la entidad es {@code null}
     */
    @Mapping(source = "userId", target = "playerId")
    InvitationModel toModel(InvitationEntity entity);

    /**
     * Convierte un modelo de invitación a su entidad JPA.
     *
     * @param model modelo fuente; puede ser {@code null}
     * @return entidad resultante, o {@code null} si el modelo es {@code null}
     */
    @Mapping(source = "playerId", target = "userId")
    InvitationEntity toEntity(InvitationModel model);

    /**
     * Convierte un modelo de dominio a un {@link InvitationResponse}.
     *
     * @param model modelo fuente; puede ser {@code null}
     * @return DTO de respuesta resultante, o {@code null} si el modelo es {@code null}
     */
    InvitationResponse toResponse(InvitationModel model);
}
