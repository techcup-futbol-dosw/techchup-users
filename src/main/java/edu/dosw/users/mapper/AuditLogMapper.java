package edu.dosw.users.mapper;

import edu.dosw.users.entity.AuditLogEntity;
import edu.dosw.users.model.AuditLogModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for bidirectional conversion between
 * {@link AuditLogEntity} and {@link AuditLogModel}.
 *
 * <p>When converting to a model, extracts the identifiers from the nested
 * relationships ({@code sportProfile.id} and {@code invitation.id}) and
 * assigns them to the flat fields of the model. When converting to an entity,
 * both relationships are ignored and must be resolved by the service layer.</p>
 */
@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    /**
     * Converts an audit log entity to its domain model, extracting the
     * identifiers from {@code sportProfile} and {@code invitation}.
     *
     * @param entity source entity; may be {@code null}
     * @return resulting model, or {@code null} if the entity is {@code null}
     */
    @Mapping(source = "sportProfile.id", target = "sportProfileId")
    @Mapping(source = "invitation.id", target = "invitationId")
    AuditLogModel toModel(AuditLogEntity entity);

    /**
     * Converts an audit log model to its JPA entity.
     * The {@code sportProfile} and {@code invitation} relationships are left
     * as {@code null} and must be assigned by the service layer.
     *
     * @param model source model; may be {@code null}
     * @return resulting entity, or {@code null} if the model is {@code null}
     */
    @Mapping(target = "sportProfile", ignore = true)
    @Mapping(target = "invitation", ignore = true)
    AuditLogEntity toEntity(AuditLogModel model);
}