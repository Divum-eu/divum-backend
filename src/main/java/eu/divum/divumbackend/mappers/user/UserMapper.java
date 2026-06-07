package eu.divum.divumbackend.mappers.user;

import eu.divum.divumbackend.domain.User;

import eu.divum.divumbackend.dtos.user.GetUserResponse;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    GetUserResponse mapToResponse(User user);
}
