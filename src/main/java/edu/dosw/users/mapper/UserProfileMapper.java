package edu.dosw.users.mapper;

import edu.dosw.users.entity.UserProfileEntity;
import edu.dosw.users.model.UserProfileModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    UserProfileModel toModel(UserProfileEntity entity);

    @Mapping(target = "sportProfile", ignore = true)
    UserProfileEntity toEntity(UserProfileModel model);
}