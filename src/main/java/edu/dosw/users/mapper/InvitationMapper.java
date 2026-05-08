package edu.dosw.users.mapper;

import edu.dosw.users.dto.InvitationResponse;
import edu.dosw.users.entity.InvitationEntity;
import edu.dosw.users.model.InvitationModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for bidirectional conversion between
 * {@link InvitationEntity} and {@link InvitationModel}.
 *
 * <p>The entity stores the player identifier in a field named {@code userId}
 * (column {@code player_id}), while the model uses {@code playerId} for
 * API compatibility. Both directions are mapped explicitly.</p>
 */
@Mapper(componentModel = "spring")
public interface InvitationMapper {

    /**
     * Converts an invitation entity to its domain model.
     *
     * @param entity source entity; may be {@code null}
     * @return resulting model, or {@code null} if the entity is {@code null}
     */
    @Mapping(source = "userId", target = "playerId")
    InvitationModel toModel(InvitationEntity entity);

    /**
     * Converts an invitation model to its JPA entity.
     *
     * @param model source model; may be {@code null}
     * @return resulting entity, or {@code null} if the model is {@code null}
     */
    @Mapping(source = "playerId", target = "userId")
    InvitationEntity toEntity(InvitationModel model);

    /** Converts a domain model to an {@link InvitationResponse}. */
    InvitationResponse toResponse(InvitationModel model);
}
