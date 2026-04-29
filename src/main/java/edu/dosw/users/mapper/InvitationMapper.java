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
 * <p>When converting to a model, extracts the player identifier
 * ({@code player.id}) and assigns it to the {@code playerId} field of the
 * model. When converting to an entity, the {@code player} relationship is
 * ignored and must be resolved by the service layer.</p>
 */
@Mapper(componentModel = "spring")
public interface InvitationMapper {

    /**
     * Converts an invitation entity to its domain model.
     *
     * @param entity source entity; may be {@code null}
     * @return resulting model, or {@code null} if the entity is {@code null}
     */
    @Mapping(source = "player.id", target = "playerId")
    InvitationModel toModel(InvitationEntity entity);

    /**
     * Converts an invitation model to its JPA entity.
     * The {@code player} relationship is left as {@code null} and must be
     * assigned by the service layer.
     *
     * @param model source model; may be {@code null}
     * @return resulting entity, or {@code null} if the model is {@code null}
     */
    @Mapping(target = "player", ignore = true)
    InvitationEntity toEntity(InvitationModel model);

    /** Converts a domain model to an {@link InvitationResponse}. */
    InvitationResponse toResponse(InvitationModel model);
}