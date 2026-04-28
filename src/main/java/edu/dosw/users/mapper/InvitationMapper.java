package edu.dosw.users.mapper;

import edu.dosw.users.entity.InvitationEntity;
import edu.dosw.users.model.InvitationModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InvitationMapper {

    @Mapping(source = "player.id", target = "playerId")
    InvitationModel toModel(InvitationEntity entity);

    @Mapping(target = "player", ignore = true)
    InvitationEntity toEntity(InvitationModel model);
}