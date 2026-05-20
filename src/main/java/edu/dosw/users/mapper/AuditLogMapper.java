package edu.dosw.users.mapper;

import edu.dosw.users.dto.AuditLogResponse;
import edu.dosw.users.entity.AuditLogEntity;
import edu.dosw.users.model.AuditLogModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper MapStruct para la conversión bidireccional entre {@link AuditLogEntity} y {@link AuditLogModel}.
 *
 * <p>Al convertir a modelo, extrae los identificadores de las relaciones anidadas
 * ({@code sportProfile.id} e {@code invitation.id}) y los asigna a los campos planos
 * del modelo. Al convertir a entidad, ambas relaciones se ignoran y deben ser resueltas
 * por la capa de servicio.</p>
 *
 * @author CodeForge
 * @since 1.0
 */
@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    /**
     * Convierte una entidad de auditoría a su modelo de dominio, extrayendo los
     * identificadores de {@code sportProfile} e {@code invitation}.
     *
     * @param entity entidad fuente; puede ser {@code null}
     * @return modelo resultante, o {@code null} si la entidad es {@code null}
     */
    @Mapping(source = "sportProfile.id", target = "sportProfileId")
    @Mapping(source = "invitation.id", target = "invitationId")
    AuditLogModel toModel(AuditLogEntity entity);

    /**
     * Convierte un modelo de auditoría a su entidad JPA.
     * Las relaciones {@code sportProfile} e {@code invitation} se dejan como
     * {@code null} y deben ser asignadas por la capa de servicio.
     *
     * @param model modelo fuente; puede ser {@code null}
     * @return entidad resultante, o {@code null} si el modelo es {@code null}
     */
    @Mapping(target = "sportProfile", ignore = true)
    @Mapping(target = "invitation", ignore = true)
    AuditLogEntity toEntity(AuditLogModel model);

    /**
     * Convierte un modelo de dominio a un {@link AuditLogResponse}.
     *
     * @param model modelo fuente; puede ser {@code null}
     * @return DTO de respuesta resultante, o {@code null} si el modelo es {@code null}
     */
    AuditLogResponse toResponse(AuditLogModel model);
}