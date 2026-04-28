package edu.dosw.users.mapper;

import edu.dosw.users.entity.SportProfileEntity;
import edu.dosw.users.model.SportProfileModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SportProfileMapper {

    @Mapping(source = "userProfile.id", target = "userId")
    SportProfileModel toModel(SportProfileEntity entity);

    @Mapping(target = "userProfile", ignore = true)
    SportProfileEntity toEntity(SportProfileModel model);
}