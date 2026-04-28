package edu.dosw.users.mapper;

import edu.dosw.users.entity.AuditLogEntity;
import edu.dosw.users.model.AuditLogModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    @Mapping(source = "sportProfile.id", target = "sportProfileId")
    @Mapping(source = "invitation.id", target = "invitationId")
    AuditLogModel toModel(AuditLogEntity entity);

    @Mapping(target = "sportProfile", ignore = true)
    @Mapping(target = "invitation", ignore = true)
    AuditLogEntity toEntity(AuditLogModel model);
}